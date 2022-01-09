package betterquesting.questing.tasks;

import betterquesting.api.questing.IQuest;
import betterquesting.api.questing.tasks.IItemTask;
import betterquesting.api.utils.BigItemStack;
import betterquesting.api.utils.ItemComparison;
import betterquesting.api.utils.JsonHelper;
import betterquesting.api2.client.gui.misc.IGuiRect;
import betterquesting.api2.client.gui.panels.IGuiPanel;
import betterquesting.api2.storage.DBEntry;
import betterquesting.api2.utils.ParticipantInfo;
import betterquesting.core.BetterQuesting;
import betterquesting.client.gui2.tasks.PanelTaskRetrieval;
import betterquesting.questing.tasks.factory.FactoryTaskRetrieval;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.Level;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public class TaskRetrieval implements ITaskInventory, IItemTask
{
	private final Set<UUID> completeUsers = new TreeSet<>();
	public final NonNullList<BigItemStack> requiredItems = NonNullList.create();
	private final TreeMap<UUID, int[]> userProgress = new TreeMap<>();
	public boolean partialMatch = true;
	public boolean ignoreNBT = false;
	public boolean consume = true;
	public boolean groupDetect = false;
	public boolean autoConsume = false;
	
	@Override
	public String getUnlocalisedName()
	{
		return BetterQuesting.MODID_STD + ".task.retrieval";
	}
	
	@Override
	public ResourceLocation getFactoryID()
	{
		return FactoryTaskRetrieval.INSTANCE.getRegistryName();
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
	public void onInventoryChange(@Nonnull DBEntry<IQuest> quest, @Nonnull ParticipantInfo pInfo)
    {
        if(!consume || autoConsume)
        {
            detect(pInfo, quest);
        }
    }
    
	@Override
	public void detect(ParticipantInfo pInfo, DBEntry<IQuest> quest)
	{
	    if(isComplete(pInfo.UUID)) return;
		
        final List<Tuple<UUID, int[]>> progress = getBulkProgress(consume ? Collections.singletonList(pInfo.UUID) : pInfo.ALL_UUIDS);
		boolean updated = false;
		
		if(!consume)
        {
            if(groupDetect) // Reset all detect progress
            {
                progress.forEach((value) -> Arrays.fill(value.getSecond(), 0));
            } else
            {
                for(int i = 0; i < requiredItems.size(); i++)
                {
                    final int r = requiredItems.get(i).stackSize;
                    for(Tuple<UUID, int[]> value : progress)
                    {
                        int n = value.getSecond()[i];
                        if(n != 0 && n < r)
                        {
                            value.getSecond()[i] = 0;
                            updated = true;
                        }
                    }
                }
            }
        }
		
		final List<InventoryPlayer> invoList;
		if(consume)
        {
            invoList = Collections.singletonList(pInfo.PLAYER.inventory);
        } else
        {
            invoList = new ArrayList<>();
            pInfo.ACTIVE_PLAYERS.forEach((p) -> invoList.add(p.inventory));
        }
		
		for(InventoryPlayer invo : invoList)
        {
            for(int i = 0; i < invo.getSizeInventory(); i++)
            {
                ItemStack stack = invo.getStackInSlot(i);
                if(stack.isEmpty()) continue;
                // Allows the stack detection to split across multiple requirements. Counts may vary per person
                int[] remCounts = new int[progress.size()];
                Arrays.fill(remCounts, stack.getCount());
                
                for(int j = 0; j < requiredItems.size(); j++)
                {
                    BigItemStack rStack = requiredItems.get(j);
                    
                    if(!ItemComparison.StackMatch(rStack.getBaseStack(), stack, !ignoreNBT, partialMatch) && !ItemComparison.OreDictionaryMatch(rStack.getOreIngredient(), rStack.GetTagCompound(), stack, !ignoreNBT, partialMatch))
                    {
                        continue;
                    }
                    
                    // Theoretically this could work in consume mode for parties but the priority order and manual submission code would need changing
                    for(int n = 0; n < progress.size(); n++)
                    {
                        Tuple<UUID, int[]> value = progress.get(n);
                        if(value.getSecond()[j] >= rStack.stackSize) continue;
                        
                        int remaining = rStack.stackSize - value.getSecond()[j];
                        
                        if(consume)
                        {
                            ItemStack removed = invo.decrStackSize(i, remaining);
                            value.getSecond()[j] += removed.getCount();
                        } else
                        {
                            int temp = Math.min(remaining, remCounts[n]);
                            remCounts[n] -= temp;
                            value.getSecond()[j] += temp;
                        }
        
                        updated = true;
                    }
                }
            }
        }
		
		if(updated) setBulkProgress(progress);
		checkAndComplete(pInfo, quest, updated);
	}
	
	private void checkAndComplete(ParticipantInfo pInfo, DBEntry<IQuest> quest, boolean resync)
    {
        final List<Tuple<UUID, int[]>> progress = getBulkProgress(consume ? Collections.singletonList(pInfo.UUID) : pInfo.ALL_UUIDS);
        boolean updated = resync;
        
        topLoop:
        for(Tuple<UUID, int[]> value : progress)
        {
            for(int j = 0; j < requiredItems.size(); j++)
            {
                if(value.getSecond()[j] >= requiredItems.get(j).stackSize) continue;
                continue topLoop;
            }
            
            updated = true;
            
            if(consume)
            {
                setComplete(value.getFirst());
            } else
            {
                progress.forEach((pair) -> setComplete(pair.getFirst()));
                break;
            }
        }
		
		if(updated)
        {
            if(consume)
            {
                pInfo.markDirty(Collections.singletonList(quest.getID()));
            } else
            {
                pInfo.markDirtyParty(Collections.singletonList(quest.getID()));
            }
        }
    }

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound json)
	{
		json.setBoolean("partialMatch", partialMatch);
		json.setBoolean("ignoreNBT", ignoreNBT);
		json.setBoolean("consume", consume);
		json.setBoolean("groupDetect", groupDetect);
		json.setBoolean("autoConsume", autoConsume);
		
		NBTTagList itemArray = new NBTTagList();
		for(BigItemStack stack : this.requiredItems)
		{
			itemArray.appendTag(JsonHelper.ItemStackToJson(stack, new NBTTagCompound()));
		}
		json.setTag("requiredItems", itemArray);
		
		return json;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt)
	{
		partialMatch = nbt.getBoolean("partialMatch");
		ignoreNBT = nbt.getBoolean("ignoreNBT");
		consume = nbt.getBoolean("consume");
		groupDetect = nbt.getBoolean("groupDetect");
		autoConsume = nbt.getBoolean("autoConsume");
		
		requiredItems.clear();
		NBTTagList iList = nbt.getTagList("requiredItems", 10);
		for(int i = 0; i < iList.tagCount(); i++)
		{
			requiredItems.add(JsonHelper.JsonToItemStack(iList.getCompoundTagAt(i)));
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
                
                int[] data = new int[requiredItems.size()];
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
	public IGuiPanel getTaskGui(IGuiRect rect, DBEntry<IQuest> quest)
	{
	    return new PanelTaskRetrieval(rect, this);
	}
	
	@Override
	public boolean canAcceptItem(UUID owner, DBEntry<IQuest> quest, ItemStack stack)
	{
		if(owner == null || stack == null || stack.isEmpty() || !consume || isComplete(owner) || requiredItems.size() <= 0)
		{
			return false;
		}
		
		int[] progress = getUsersProgress(owner);
		
		for(int j = 0; j < requiredItems.size(); j++)
		{
			BigItemStack rStack = requiredItems.get(j);
			
			if(progress[j] >= rStack.stackSize) continue;
			
			if(ItemComparison.StackMatch(rStack.getBaseStack(), stack, !ignoreNBT, partialMatch) || ItemComparison.OreDictionaryMatch(rStack.getOreIngredient(), rStack.GetTagCompound(), stack, !ignoreNBT, partialMatch))
			{
				return true;
			}
		}
		
		return false;
	}
	
	@Override
	public ItemStack submitItem(UUID owner, DBEntry<IQuest> quest, ItemStack input)
	{
		if(owner == null || input.isEmpty() || !consume || isComplete(owner)) return input;
		
		ItemStack stack = input.copy();
		
		int[] progress = getUsersProgress(owner);
		boolean updated = false;
		
		for(int j = 0; j < requiredItems.size(); j++)
		{
			if(stack.isEmpty()) break;
			
			BigItemStack rStack = requiredItems.get(j);
			
			if(progress[j] >= rStack.stackSize) continue;

			int remaining = rStack.stackSize - progress[j];
			
			if(ItemComparison.StackMatch(rStack.getBaseStack(), stack, !ignoreNBT, partialMatch) || ItemComparison.OreDictionaryMatch(rStack.getOreIngredient(), rStack.GetTagCompound(), stack, !ignoreNBT, partialMatch))
			{
				int removed = Math.min(stack.getCount(), remaining);
				stack.shrink(removed);
				progress[j] += removed;
				updated = true;
				if(stack.isEmpty()) break;
			}
		}
		
		if(updated)
        {
            setUserProgress(owner, progress);
    
            MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
            EntityPlayerMP player = server == null ? null : server.getPlayerList().getPlayerByUUID(owner);
            
            if(player != null)
            {
                checkAndComplete(new ParticipantInfo(player), quest, true);
            } else
            {
                // It's implied to be a consume task so no need to lookup the party
                boolean hasAll = true;
                for(int j = 0; j < requiredItems.size(); j++)
                {
                    if(progress[j] >= requiredItems.get(j).stackSize) continue;
                    
                    hasAll = false;
                    break;
                }
                
                if(hasAll) setComplete(owner);
            }
        }
		
		return stack.isEmpty() ? ItemStack.EMPTY : stack;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public GuiScreen getTaskEditor(GuiScreen parent, DBEntry<IQuest> quest)
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
		return progress == null || progress.length != requiredItems.size()? new int[requiredItems.size()] : progress;
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
