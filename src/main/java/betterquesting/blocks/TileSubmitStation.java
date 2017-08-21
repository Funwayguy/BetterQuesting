package betterquesting.blocks;

import java.util.UUID;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import org.apache.logging.log4j.Level;
import betterquesting.api.network.QuestingPacket;
import betterquesting.api.properties.NativeProps;
import betterquesting.api.questing.IQuest;
import betterquesting.api.questing.tasks.IFluidTask;
import betterquesting.api.questing.tasks.IItemTask;
import betterquesting.api.questing.tasks.ITask;
import betterquesting.core.BetterQuesting;
import betterquesting.network.PacketSender;
import betterquesting.network.PacketTypeNative;
import betterquesting.questing.QuestDatabase;
import betterquesting.storage.QuestSettings;

public class TileSubmitStation extends TileEntity implements IFluidHandler, ISidedInventory, ITickable, IFluidTankProperties
{
	private final IItemHandler itemHandler;
	private final IFluidHandler fluidHandler;
	private NonNullList<ItemStack> itemStack = NonNullList.<ItemStack>withSize(2, ItemStack.EMPTY);
	boolean needsUpdate = false;
	public UUID owner;
	public int questID;
	public int taskID;
	
	public TileSubmitStation()
	{
		super();
		
		this.itemHandler = new SSItemHandler(this);
		this.fluidHandler = this;
	}
	
	public IQuest getQuest()
	{
		if(questID < 0)
		{
			return null;
		} else
		{
			return QuestDatabase.INSTANCE.getValue(questID);
		}
	}
	
	public ITask getRawTask()
	{
		IQuest q = getQuest();
		
		if(q == null || taskID < 0 || taskID >= q.getTasks().size())
		{
			return null;
		} else
		{
			return q.getTasks().getValue(taskID);
		}
	}
	
	public IItemTask getItemTask()
	{
		ITask t = getRawTask();
		return t == null? null : (t instanceof IItemTask? (IItemTask)t : null);
	}
	
	public IFluidTask getFluidTask()
	{
		ITask t = getRawTask();
		return t == null? null : (t instanceof IFluidTask? (IFluidTask)t : null);
	}
	
	@Override
	public int getSizeInventory()
	{
		return 2;
	}

	@Override
	public ItemStack getStackInSlot(int idx)
	{
		if(idx < 0 || idx >= itemStack.size())
		{
			return ItemStack.EMPTY;
		} else
		{
			return itemStack.get(idx);
		}
	}

	@Override
	public ItemStack decrStackSize(int idx, int amount)
	{
		return ItemStackHelper.getAndSplit(itemStack, idx, amount);
	}

	@Override
	public void setInventorySlotContents(int idx, ItemStack stack)
	{
		if(idx < 0 || idx >= itemStack.size())
		{
			return;
		}
		
		itemStack.set(idx, stack);
	}

	@Override
	public String getName()
	{
		return BetterQuesting.submitStation.getLocalizedName();
	}

	@Override
	public boolean hasCustomName()
	{
		return false;
	}

	@Override
	public int getInventoryStackLimit()
	{
		return 64;
	}

	@Override
	public boolean isUsableByPlayer(EntityPlayer player)
	{
		if(owner == null || player.getUniqueID().equals(owner))
		{
			return true;
		}
		
		return false;
	}

	@Override
	public void openInventory(EntityPlayer player)
	{
	}

	@Override
	public void closeInventory(EntityPlayer player)
	{
	}

	@Override
	public boolean isItemValidForSlot(int idx, ItemStack stack)
	{
		if(idx != 0)
		{
			return false;
		}
		
		IItemTask t = getItemTask();
		
		return t != null && itemStack.get(idx).isEmpty() && !((ITask)t).isComplete(owner) && t.canAcceptItem(owner, stack);
	}

	@Override
	public int fill(FluidStack fluid, boolean doFill)
	{
		IQuest q = getQuest();
		IFluidTask t = getFluidTask();
		
		if(q == null || t == null || fluid == null)
		{
			return 0;
		}
		
		FluidStack remainder = null;
		int amount = fluid.amount;
		
		if(doFill)
		{
			remainder = t.submitFluid(owner, fluid);
		
			if(((ITask)t).isComplete(owner))
			{
				PacketSender.INSTANCE.sendToAll(q.getSyncPacket());
				reset();
				world.getMinecraftServer().getPlayerList().sendToAllNearExcept(null, pos.getX(), pos.getY(), pos.getZ(), 128, world.provider.getDimension(), getUpdatePacket());
			} else
			{
				needsUpdate = true;
			}
		}
		
		return remainder != null? amount - remainder.amount : amount;
	}

	@Override
	public FluidStack drain(FluidStack resource, boolean doDrain)
	{
		return null;
	}

	@Override
	public FluidStack drain(int maxDrain, boolean doDrain)
	{
		return null;
	}
	
	@Override
	public boolean canFill()
	{
		return true;
	}

	@Override
	public boolean canFillFluidType(FluidStack fluid)
	{
		IFluidTask t = getFluidTask();
		
		return t != null && !((ITask)t).isComplete(owner) && t.canAcceptFluid(owner, new FluidStack(fluid, 1));
	}
	
	@Override
	public boolean canDrain()
	{
		return false;
	}

	@Override
	public boolean canDrainFluidType(FluidStack fluid)
	{
		return false;
	}
	
	@Override
	public int getCapacity()
	{
		return Integer.MAX_VALUE;
	}
	
	@Override
	public FluidStack getContents()
	{
		return null;
	}

	@Override
	public IFluidTankProperties[] getTankProperties()
	{
		return new IFluidTankProperties[]{this};
	}
	
	@Override
	public void update()
	{
		if(world.isRemote || QuestSettings.INSTANCE.getProperty(NativeProps.EDIT_MODE))
		{
			return;
		}
		
		IQuest q = getQuest();
		IItemTask t = getItemTask();
		
		if(world.getTotalWorldTime()%10 == 0)
		{
			if(owner != null && q != null && t != null && owner != null && !itemStack.get(0).isEmpty() && itemStack.get(1).isEmpty())
			{
				ItemStack inStack = itemStack.get(0).copy();
				
				if(t.canAcceptItem(owner, inStack))
				{
					itemStack.set(0, t.submitItem(owner, inStack)); // Even if this returns an invalid item for submission it will be moved next pass
					
					if(((ITask)t).isComplete(owner))
					{
						PacketSender.INSTANCE.sendToAll(q.getSyncPacket());
						reset();
						world.getMinecraftServer().getPlayerList().sendToAllNearExcept(null, pos.getX(), pos.getY(), pos.getZ(), 128, world.provider.getDimension(), getUpdatePacket());
					} else
					{
						needsUpdate = true;
					}
				} else
				{
					itemStack.set(1, inStack);
					itemStack.set(0, ItemStack.EMPTY);
				}
			}
			
			if(needsUpdate)
			{
				needsUpdate = false;
				
				if(q != null && !world.isRemote)
				{
					PacketSender.INSTANCE.sendToAll(q.getSyncPacket());
				}
			} else if(t != null && ((ITask)t).isComplete(owner))
			{
				reset();
				world.getMinecraftServer().getPlayerList().sendToAllNearExcept(null, pos.getX(), pos.getY(), pos.getZ(), 128, world.provider.getDimension(), getUpdatePacket());
			}
		}
	}
	
	public void setupTask(UUID owner, IQuest quest, ITask task)
	{
		if(owner == null || quest == null || task == null)
		{
			reset();
			return;
		}
		
		this.questID = QuestDatabase.INSTANCE.getKey(quest);
		this.taskID = quest.getTasks().getKey(task);
		
		if(this.questID < 0 || this.taskID < 0)
		{
			reset();
			return;
		}
		
		this.owner = owner;
		this.markDirty();
	}
	
	public boolean isSetup()
	{
		return owner != null && questID < 0 && taskID < 0;
	}
	
	public void reset()
	{
		owner = null;
		questID = -1;
		taskID = -1;
		this.markDirty();
	}

    /**
     * Overridden in a sign to provide the text.
     */
	@Override
    public SPacketUpdateTileEntity getUpdatePacket()
    {
        NBTTagCompound nbttagcompound = new NBTTagCompound();
        this.writeToNBT(nbttagcompound);
        return new SPacketUpdateTileEntity(pos, 0, nbttagcompound);
    }

    /**
     * Called when you receive a TileEntityData packet for the location this
     * TileEntity is currently in. On the client, the NetworkManager will always
     * be the remote server. On the server, it will be whomever is responsible for
     * sending the packet.
     *
     * @param net The NetworkManager the packet originated from
     * @param pkt The data packet
     */
    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt)
    {
    	this.readFromNBT(pkt.getNbtCompound());
    }
    
    /**
     * Ignores parameter on client side (uses own data instead)
     */
    public void SyncTile(NBTTagCompound data)
    {
    	if(!world.isRemote)
    	{
    		this.readFromNBT(data);
    		this.markDirty();
    		world.getMinecraftServer().getPlayerList().sendToAllNearExcept(null, pos.getX(), pos.getY(), pos.getZ(), 128, world.provider.getDimension(), getUpdatePacket());
    	} else
    	{
    		NBTTagCompound payload = new NBTTagCompound();
    		NBTTagCompound tileData = new NBTTagCompound();
    		this.writeToNBT(tileData);
    		payload.setTag("tile", tileData);
    		PacketSender.INSTANCE.sendToServer(new QuestingPacket(PacketTypeNative.EDIT_STATION.GetLocation(), payload));
    	}
    }
	
	@Override
	public void readFromNBT(NBTTagCompound tags)
	{
		super.readFromNBT(tags);
		
		ItemStackHelper.loadAllItems(tags.getCompoundTag("inventory"), itemStack);
		
		try
		{
			owner = UUID.fromString(tags.getString("owner"));
		} catch(Exception e)
		{
			this.reset();
			return;
		}
		
		questID = tags.hasKey("questID")? tags.getInteger("questID") : -1;
		taskID = tags.hasKey("task")? tags.getInteger("task") : -1;
		
		if(isSetup()) // All data must be present for this to run correctly
		{
			BetterQuesting.logger.log(Level.ERROR, "One or more tags were missing!", new Exception());
			this.reset();
		}
	}
	
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound tags)
	{
		super.writeToNBT(tags);
		tags.setString("owner", owner != null? owner.toString() : "");
		tags.setInteger("questID", questID);
		tags.setInteger("task", taskID);
		
		tags.setTag("inventory", ItemStackHelper.saveAllItems(new NBTTagCompound(), itemStack));
		
		return tags;
	}

	@Override
	public int[] getSlotsForFace(EnumFacing side)
	{
		return new int[]{0,1};
	}

	@Override
	public boolean canInsertItem(int slot, ItemStack stack, EnumFacing side)
	{
		return isItemValidForSlot(slot, stack);
	}

	@Override
	public boolean canExtractItem(int slot, ItemStack stack, EnumFacing side)
	{
		return slot == 1;
	}

	@Override
	public ItemStack removeStackFromSlot(int index)
	{
		return ItemStackHelper.getAndRemove(itemStack, index);
	}

	@Override
	public int getField(int id)
	{
		return 0;
	}

	@Override
	public void setField(int id, int value)
	{
	}

	@Override
	public int getFieldCount()
	{
		return 0;
	}

	@Override
	public void clear()
	{
		itemStack.clear();
	}

	@Override
	public ITextComponent getDisplayName()
	{
		return new TextComponentString(BetterQuesting.submitStation.getLocalizedName());
	}
	
	@Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing)
    {
		if(capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
		{
			return true;
		} else if(capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY)
		{
			return true;
		}
		
        return super.hasCapability(capability, facing);
    }
	
	@Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing)
    {
		if(capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
		{
			return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(itemHandler);
		} else if(capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY)
		{
			return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(fluidHandler);
		}
		
        return super.getCapability(capability, facing);
    }

	@Override
	public boolean isEmpty()
	{
		return itemStack.isEmpty();
	}
}
