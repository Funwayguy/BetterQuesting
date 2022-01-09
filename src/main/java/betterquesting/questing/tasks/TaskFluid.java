package betterquesting.questing.tasks;

import betterquesting.api.questing.IQuest;
import betterquesting.api.questing.tasks.IFluidTask;
import betterquesting.api.questing.tasks.IItemTask;
import betterquesting.api.utils.JsonHelper;
import betterquesting.api2.client.gui.misc.IGuiRect;
import betterquesting.api2.client.gui.panels.IGuiPanel;
import betterquesting.api2.storage.DBEntry;
import betterquesting.api2.utils.ParticipantInfo;
import betterquesting.core.BetterQuesting;
import betterquesting.client.gui2.tasks.PanelTaskFluid;
import betterquesting.questing.tasks.factory.FactoryTaskFluid;
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
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.Level;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public class TaskFluid implements ITaskInventory, IFluidTask, IItemTask
{
	private final Set<UUID> completeUsers = new TreeSet<>();
	public final NonNullList<FluidStack> requiredFluids = NonNullList.create();
	public final TreeMap<UUID, int[]> userProgress = new TreeMap<>();
	//public boolean partialMatch = true; // Not many ideal ways of implementing this with fluid handlers
	public boolean ignoreNbt = false;
	public boolean consume = true;
	public boolean groupDetect = false;
	public boolean autoConsume = false;
	
	@Override
	public ResourceLocation getFactoryID()
	{
		return FactoryTaskFluid.INSTANCE.getRegistryName();
	}
	
	@Override
	public String getUnlocalisedName()
	{
		return "bq_standard.task.fluid";
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
	    
	    // Removing the consume check here would make the task cheaper on groups and for that reason sharing is restricted to detect only
        final List<Tuple<UUID, int[]>> progress = getBulkProgress(consume ? Collections.singletonList(pInfo.UUID) : pInfo.ALL_UUIDS);
		boolean updated = false;
		
        if(!consume)
        {
            if(groupDetect) // Reset all detect progress
            {
                progress.forEach((value) -> Arrays.fill(value.getSecond(), 0));
            } else
            {
                for(int i = 0; i < requiredFluids.size(); i++)
                {
                    final int r = requiredFluids.get(i).amount;
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
            // We do not support consuming resources from other member's invetories.
            // This could otherwise be abused to siphon items/fluids unknowingly
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
                IFluidHandlerItem handler = FluidUtil.getFluidHandler(stack);
                if(handler == null) continue;
    
                boolean hasDrained = false;
    
                for(int j = 0; j < requiredFluids.size(); j++)
                {
                    final FluidStack rStack = requiredFluids.get(j);
                    FluidStack drainOG = rStack.copy();
                    if(ignoreNbt) drainOG.tag = null;
                    
                    // Pre-check
                    FluidStack sample = handler.drain(drainOG, false);
                    if(sample == null || sample.amount <= 0) continue;
        
                    // Theoretically this could work in consume mode for parties but the priority order and manual submission code would need changing
                    for(Tuple<UUID, int[]> value : progress)
                    {
                        if(value.getSecond()[j] >= rStack.amount) continue;
                        int remaining = rStack.amount - value.getSecond()[j];
            
                        FluidStack drain = rStack.copy();
                        drain.amount = remaining / stack.getCount(); // Must be a multiple of the stack size
                        if(ignoreNbt) drain.tag = null;
                        if(drain.amount <= 0) continue;
            
                        FluidStack fluid = handler.drain(drain, consume); // TODO: Look into reducing this to a single call if possible
                        if(fluid == null || fluid.amount <= 0) continue;
            
                        value.getSecond()[j] += fluid.amount * stack.getCount();
                        hasDrained = true;
                        updated = true;
                    }
                }
    
                if(hasDrained && consume) invo.setInventorySlotContents(i, handler.getContainer());
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
            for(int j = 0; j < requiredFluids.size(); j++)
            {
                if(value.getSecond()[j] >= requiredFluids.get(j).amount) continue;
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
	public NBTTagCompound writeToNBT(NBTTagCompound nbt)
	{
	    //json.setBoolean("partialMatch", partialMatch);
		nbt.setBoolean("ignoreNBT", ignoreNbt);
		nbt.setBoolean("consume", consume);
		nbt.setBoolean("groupDetect", groupDetect);
		nbt.setBoolean("autoConsume", autoConsume);
		
		NBTTagList itemArray = new NBTTagList();
		for(FluidStack stack : this.requiredFluids)
		{
			itemArray.appendTag(stack.writeToNBT(new NBTTagCompound()));
		}
		nbt.setTag("requiredFluids", itemArray);
		
		return nbt;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt)
	{
	    //partialMatch = json.getBoolean("partialMatch");
		ignoreNbt = nbt.getBoolean("ignoreNBT");
		consume = nbt.getBoolean("consume");
		groupDetect = nbt.getBoolean("groupDetect");
		autoConsume = nbt.getBoolean("autoConsume");
		
		requiredFluids.clear();
		NBTTagList fList = nbt.getTagList("requiredFluids", 10);
		for(int i = 0; i < fList.tagCount(); i++)
		{
			requiredFluids.add(JsonHelper.JsonToFluidStack(fList.getCompoundTagAt(i)));
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
                
                int[] data = new int[requiredFluids.size()];
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
	    return new PanelTaskFluid(rect, this);
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public GuiScreen getTaskEditor(GuiScreen screen, DBEntry<IQuest> quest)
	{
		return null;
	}

	@Override
	public boolean canAcceptFluid(UUID owner, DBEntry<IQuest> quest, FluidStack fluid)
	{
		if(owner == null || fluid == null || fluid.getFluid() == null || !consume || isComplete(owner) || requiredFluids.size() <= 0)
		{
			return false;
		}
		
		int[] progress = getUsersProgress(owner);
		
		for(int j = 0; j < requiredFluids.size(); j++)
		{
			FluidStack rStack = requiredFluids.get(j).copy();
			if(ignoreNbt) rStack.tag = null;
			if(progress[j] < rStack.amount && rStack.equals(fluid)) return true;
		}
		
		return false;
	}

	@Override
	public boolean canAcceptItem(UUID owner, DBEntry<IQuest> quest, ItemStack item)
	{
		if(owner == null || item == null || item.isEmpty() || !consume || isComplete(owner) || requiredFluids.size() <= 0)
		{
			return false;
		}
		
		IFluidHandlerItem handler = FluidUtil.getFluidHandler(item);
		
		if(handler == null) return false;
		
		for(IFluidTankProperties tank : handler.getTankProperties())
        {
            if(!tank.canDrain()) continue;
            
            for(FluidStack rStack : requiredFluids)
            {
                if(rStack.equals(tank.getContents())) return true;
            }
        }
		
		return false;
	}

	@Override
	public FluidStack submitFluid(UUID owner, DBEntry<IQuest> quest, FluidStack fluid)
	{
		return submitFluidInternal(owner, quest, fluid, true);
	}
	
	private FluidStack submitFluidInternal(UUID owner, DBEntry<IQuest> quest, FluidStack fluid, boolean doFill)
	{
		if(owner == null || fluid == null || fluid.amount <= 0 || !consume || isComplete(owner) || requiredFluids.size() <= 0)
		{
			return fluid;
		}
		
		int[] progress = getUsersProgress(owner).clone();
		boolean updated = false;
		
		for(int j = 0; j < requiredFluids.size(); j++)
		{
			FluidStack rStack = requiredFluids.get(j);
			
			if(progress[j] >= rStack.amount) continue;
			
			int remaining = rStack.amount - progress[j];
			
			if(rStack.isFluidEqual(fluid))
			{
				int removed = Math.min(fluid.amount, remaining);
				progress[j] += removed;
				fluid.amount -= removed;
				updated = true;
				
				if(fluid.amount <= 0)
				{
					fluid = null;
					break;
				}
			}
		}
		
		if(updated && doFill)
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
                for(int j = 0; j < requiredFluids.size(); j++)
                {
                    if(progress[j] >= requiredFluids.get(j).amount) continue;
                    
                    hasAll = false;
                    break;
                }
                
                if(hasAll) setComplete(owner);
            }
        }
		
		return fluid;
	}

	@Override
	public ItemStack submitItem(UUID owner, DBEntry<IQuest> quest, ItemStack input)
	{
		if(owner == null || input.isEmpty() || !consume || isComplete(owner)) return input;
		
		ItemStack item = input.splitStack(1); // Prevents issues with stack filling/draining

		IFluidHandlerItem handler = FluidUtil.getFluidHandler(item);
		if(handler == null) return item;
		
        boolean hasDrained = false;
        
        for(IFluidTankProperties tank : handler.getTankProperties())
        {
            if(!tank.canDrain() || tank.getContents() == null || !tank.canDrainFluidType(tank.getContents())) continue;
            
            // Figure out how much of this fluid is left to submit to the task
            FluidStack remaining = submitFluidInternal(owner, quest, tank.getContents().copy(), false);
            FluidStack drain = tank.getContents().copy();
            drain.amount -= remaining == null ? 0 : remaining.amount;
            
            if(drain.amount <= 0) continue;
            
            // Attempt drain of remaining amount and submit to task progress
            submitFluidInternal(owner, quest, handler.drain(drain, true), true);
            hasDrained = true;
        }
		
		return hasDrained ? handler.getContainer() : item;
	}
 
	private void setUserProgress(UUID uuid, int[] progress)
	{
		userProgress.put(uuid, progress);
	}
 
	public int[] getUsersProgress(UUID uuid)
	{
		int[] progress = userProgress.get(uuid);
		return progress == null || progress.length != requiredFluids.size()? new int[requiredFluids.size()] : progress;
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
