package betterquesting.questing.tasks;

import betterquesting.api.questing.IQuest;
import betterquesting.api.questing.tasks.ITask;
import betterquesting.api.utils.ItemComparison;
import betterquesting.api2.client.gui.misc.IGuiRect;
import betterquesting.api2.client.gui.panels.IGuiPanel;
import betterquesting.api2.storage.DBEntry;
import betterquesting.api2.utils.ParticipantInfo;
import betterquesting.core.BetterQuesting;
import betterquesting.NbtBlockType;
import betterquesting.client.gui2.tasks.PanelTaskBlockBreak;
import betterquesting.questing.tasks.factory.FactoryTaskBlockBreak;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;
import org.apache.logging.log4j.Level;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public class TaskBlockBreak implements ITask
{
	private final Set<UUID> completeUsers = new TreeSet<>();
	private final TreeMap<UUID, int[]> userProgress = new TreeMap<>();
	public final List<NbtBlockType> blockTypes = new ArrayList<>();
	
	public TaskBlockBreak()
	{
		blockTypes.add(new NbtBlockType());
	}
	
	@Override
	public ResourceLocation getFactoryID()
	{
		return FactoryTaskBlockBreak.INSTANCE.getRegistryName();
	}
	
	@Override
	public boolean isComplete(UUID uuid)
	{
		return completeUsers.contains(uuid);
	}
	
	@Override
	public void setComplete(UUID uuid)
	{
		completeUsers.add(uuid);
	}
	
	@Override
	public String getUnlocalisedName()
	{
		return "bq_standard.task.block_break";
	}
	
	@Override
	public void detect(ParticipantInfo pInfo, DBEntry<IQuest> quest)
	{
	    pInfo.ALL_UUIDS.forEach((uuid) -> {
            if(isComplete(uuid)) return;
            
            int[] tmp = getUsersProgress(uuid);
            for(int i = 0; i < blockTypes.size(); i++)
            {
                NbtBlockType block = blockTypes.get(i);
                if(block != null && tmp[i] < block.n) return;
            }
            setComplete(uuid);
        });
	    
	    pInfo.markDirtyParty(Collections.singletonList(quest.getID()));
	}
	
	public void onBlockBreak(ParticipantInfo pInfo, DBEntry<IQuest> quest, IBlockState state, BlockPos pos)
	{
		TileEntity tile = state.getBlock().hasTileEntity(state) ? pInfo.PLAYER.world.getTileEntity(pos) : null;
		NBTTagCompound tags = tile == null ? null : tile.writeToNBT(new NBTTagCompound());
		
        final List<Tuple<UUID, int[]>> progress = getBulkProgress(pInfo.ALL_UUIDS);
        boolean changed = false;
		
		for(int i = 0; i < blockTypes.size(); i++)
		{
			NbtBlockType targetBlock = blockTypes.get(i);
			
			int tmpMeta = (targetBlock.m < 0 || targetBlock.m == OreDictionary.WILDCARD_VALUE)? OreDictionary.WILDCARD_VALUE : state.getBlock().getMetaFromState(state);
			boolean oreMatch = targetBlock.oreDict.length() > 0 && OreDictionary.getOres(targetBlock.oreDict).contains(new ItemStack(state.getBlock(), 1, tmpMeta));
			final int index = i;
			
			if((oreMatch || (state.getBlock() == targetBlock.b && (targetBlock.m < 0 || state.getBlock().getMetaFromState(state) == targetBlock.m))) && ItemComparison.CompareNBTTag(targetBlock.tags, tags, true))
			{
			    progress.forEach((entry) -> {
			        if(entry.getSecond()[index] >= targetBlock.n) return;
			        entry.getSecond()[index]++;
                });
			    changed = true;
				break; // NOTE: We're only tracking one break at a time so doing all the progress setting above is fine
			}
		}
		
		if(changed)
        {
            setBulkProgress(progress);
            detect(pInfo, quest);
        }
	}
	
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt)
	{
		NBTTagList bAry = new NBTTagList();
		for(NbtBlockType block : blockTypes)
		{
			bAry.appendTag(block.writeToNBT(new NBTTagCompound()));
		}
		nbt.setTag("blocks", bAry);
		
		return nbt;
	}
	
	@Override
	public void readFromNBT(NBTTagCompound nbt)
	{
		blockTypes.clear();
		NBTTagList bList = nbt.getTagList("blocks", 10);
		for(int i = 0; i < bList.tagCount(); i++)
		{
			NbtBlockType block = new NbtBlockType();
			block.readFromNBT(bList.getCompoundTagAt(i));
			blockTypes.add(block);
		}
		
		if(nbt.hasKey("blockID", 8))
		{
			Block targetBlock = Block.REGISTRY.getObject(new ResourceLocation(nbt.getString("blockID")));
			targetBlock = targetBlock != Blocks.AIR ? targetBlock : Blocks.LOG;
			int targetMeta = nbt.getInteger("blockMeta");
			NBTTagCompound targetNbt = nbt.getCompoundTag("blockNBT");
			int targetNum = nbt.getInteger("amount");
			
			NbtBlockType leg = new NbtBlockType();
			leg.b = targetBlock;
			leg.m = targetMeta;
			leg.tags = targetNbt;
			leg.n = targetNum;
			
			blockTypes.add(leg);
		}
	}
	
	@Override
	public void readProgressFromNBT(NBTTagCompound nbt, boolean merge)
	{
		if(!merge)
        {
            completeUsers.clear();
            userProgress.clear();
        }
		
		NBTTagList cList = nbt.getTagList("completeUsers", 8);
		for(int i = 0; i < cList.tagCount(); i++)
		{
			try
			{
				completeUsers.add(UUID.fromString(cList.getStringTagAt(i)));
			} catch(Exception e)
			{
                BetterQuesting.logger.log(Level.ERROR, "Unable to load UUID for task", e);
			}
		}
		
		NBTTagList pList = nbt.getTagList("userProgress", 10);
		for(int n = 0; n < pList.tagCount(); n++)
		{
			try
			{
                NBTTagCompound pTag = pList.getCompoundTagAt(n);
                UUID uuid = UUID.fromString(pTag.getString("uuid"));
                
                int[] data = new int[blockTypes.size()];
                NBTTagList dNbt = pTag.getTagList("data", 3);
                for(int i = 0; i < data.length && i < dNbt.tagCount(); i++) // TODO: Change this to an int array. This is dumb...
                {
                    data[i] = dNbt.getIntAt(i);
                }
                
			    userProgress.put(uuid, data);
			} catch(Exception e)
			{
                BetterQuesting.logger.log(Level.ERROR, "Unable to load user progress for task", e);
			}
		}
	}
	
	@Override
	public NBTTagCompound writeProgressToNBT(NBTTagCompound nbt, @Nullable List<UUID> users)
	{
		NBTTagList jArray = new NBTTagList();
		NBTTagList progArray = new NBTTagList();
		
		if(users != null)
        {
            users.forEach((uuid) -> {
                if(completeUsers.contains(uuid)) jArray.appendTag(new NBTTagString(uuid.toString()));
                
                int[] data = userProgress.get(uuid);
                if(data != null)
                {
                    NBTTagCompound pJson = new NBTTagCompound();
                    pJson.setString("uuid", uuid.toString());
                    NBTTagList pArray = new NBTTagList(); // TODO: Why the heck isn't this just an int array?!
                    for(int i : data) pArray.appendTag(new NBTTagInt(i));
                    pJson.setTag("data", pArray);
                    progArray.appendTag(pJson);
                }
            });
        } else
        {
            completeUsers.forEach((uuid) -> jArray.appendTag(new NBTTagString(uuid.toString())));
            
            userProgress.forEach((uuid, data) -> {
                NBTTagCompound pJson = new NBTTagCompound();
			    pJson.setString("uuid", uuid.toString());
                NBTTagList pArray = new NBTTagList(); // TODO: Why the heck isn't this just an int array?!
                for(int i : data) pArray.appendTag(new NBTTagInt(i));
                pJson.setTag("data", pArray);
                progArray.appendTag(pJson);
            });
        }
		
		nbt.setTag("completeUsers", jArray);
		nbt.setTag("userProgress", progArray);
		
		return nbt;
	}
	
	@Override
	public void resetUser(@Nullable UUID uuid)
	{
	    if(uuid == null)
        {
            completeUsers.clear();
            userProgress.clear();
        } else
        {
            completeUsers.remove(uuid);
            userProgress.remove(uuid);
        }
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IGuiPanel getTaskGui(IGuiRect rect, DBEntry<IQuest> quest)
	{
	    return new PanelTaskBlockBreak(rect, this);
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public GuiScreen getTaskEditor(GuiScreen screen, DBEntry<IQuest> context)
	{
		return null;
	}
	
	private void setUserProgress(UUID uuid, int[] progress)
	{
		userProgress.put(uuid, progress);
	}
	
	public int[] getUsersProgress(UUID uuid)
	{
		int[] progress = userProgress.get(uuid);
		return progress == null || progress.length != blockTypes.size()? new int[blockTypes.size()] : progress;
	}
	
	private List<Tuple<UUID, int[]>> getBulkProgress(@Nonnull List<UUID> uuids)
    {
        if(uuids.size() <= 0) return Collections.emptyList();
        List<Tuple<UUID, int[]>> list = new ArrayList<>();
        uuids.forEach((key) -> list.add(new Tuple<>(key, getUsersProgress(key))));
        return list;
    }
    
    private void setBulkProgress(@Nonnull List<Tuple<UUID, int[]>> list)
    {
        list.forEach((entry) -> setUserProgress(entry.getFirst(), entry.getSecond()));
    }
}
