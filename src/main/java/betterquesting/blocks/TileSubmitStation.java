package betterquesting.blocks;

import java.util.UUID;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.fluids.capability.TileFluidHandler;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import org.apache.logging.log4j.Level;
import betterquesting.core.BetterQuesting;
import betterquesting.network.PacketAssembly;
import betterquesting.network.PacketTypeRegistry.BQPacketType;
import betterquesting.quests.QuestDatabase;
import betterquesting.quests.QuestInstance;
import betterquesting.quests.tasks.TaskBase;
import betterquesting.quests.tasks.advanced.IContainerTask;

public class TileSubmitStation extends TileFluidHandler implements IFluidHandler, ISidedInventory, ITickable, IFluidTankProperties
{
	private final IItemHandler itemHandler;
	private ItemStack[] itemStack = new ItemStack[2];
	boolean needsUpdate = false;
	public UUID owner;
	public int questID;
	public int taskID;
	
	public TileSubmitStation()
	{
		this.itemHandler = new SSItemHandler(this);
	}
	
	public QuestInstance getQuest()
	{
		if(questID < 0)
		{
			return null;
		} else
		{
			return QuestDatabase.getQuestByID(questID);
		}
	}
	
	public IContainerTask getTask()
	{
		QuestInstance q = getQuest();
		
		if(q == null || taskID < 0 || taskID >= q.tasks.size())
		{
			return null;
		} else
		{
			TaskBase t = q.tasks.get(taskID);
			return t instanceof IContainerTask? (IContainerTask)t : null;
		}
	}
	
	@Override
	public int getSizeInventory()
	{
		return 2;
	}

	@Override
	public ItemStack getStackInSlot(int idx)
	{
		if(idx < 0 || idx >= itemStack.length)
		{
			return null;
		} else
		{
			return itemStack[idx];
		}
	}

	@Override
	public ItemStack decrStackSize(int idx, int amount)
	{
		if(idx < 0 || idx >= itemStack.length || itemStack[idx] == null)
		{
			return null;
		}
		
        if (amount >= itemStack[idx].stackSize)
        {
            ItemStack itemstack = itemStack[idx];
            itemStack[idx] = null;
            return itemstack;
        }
        else
        {
            itemStack[idx].stackSize -= amount;
            ItemStack cpy = itemStack[idx].copy();
            cpy.stackSize = amount;
            return cpy;
        }
	}

	@Override
	public void setInventorySlotContents(int idx, ItemStack stack)
	{
		if(idx < 0 || idx >= itemStack.length)
		{
			return;
		}
		
		itemStack[idx] = stack;
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
	public boolean isUseableByPlayer(EntityPlayer player)
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
		
		IContainerTask t = getTask();
		
		return t != null && itemStack[1] == null && !((TaskBase)t).isComplete(owner) && t.canAcceptItem(owner, stack);
	}
	
	@Override
	public IFluidTankProperties[] getTankProperties()
	{
		return new IFluidTankProperties[]{this};
	}
	
	@Override
	public int fill(FluidStack fluid, boolean doFill)
	{
		QuestInstance q = getQuest();
		IContainerTask t = getTask();
		
		if(q == null || t == null || fluid == null)
		{
			return 0;
		}
		
		FluidStack remainder = null;
		int amount = fluid.amount;
		
		if(doFill)
		{
			remainder = t.submitFluid(owner, fluid);
		
			if(((TaskBase)t).isComplete(owner))
			{
				q.UpdateClients();
				reset();
				worldObj.getMinecraftServer().getPlayerList().sendToAllNearExcept(null, pos.getX(), pos.getY(), pos.getZ(), 128, worldObj.provider.getDimension(), getUpdatePacket());
			} else
			{
				needsUpdate = true;
			}
		}
		
		return remainder != null? amount - remainder.amount : amount;
	}
	
	@Override
	public FluidStack drain(FluidStack fluid, boolean doDrain)
	{
		return null;
	}
	
	@Override
	public FluidStack drain(int maxDrain, boolean doDrain)
	{
		return null;
	}
	
	@Override
	public FluidStack getContents()
	{
		return null;
	}
	
	@Override
	public int getCapacity()
	{
		return Integer.MAX_VALUE;
	}
	
	@Override
	public boolean canFill()
	{
		return true;
	}
	
	@Override
	public boolean canDrain()
	{
		return false;
	}
	
	@Override
	public boolean canFillFluidType(FluidStack fluid)
	{
		IContainerTask t = getTask();
		
		return t != null && fluid != null && !((TaskBase)t).isComplete(owner) && t.canAcceptFluid(owner, fluid.getFluid());
	}
	
	@Override
	public boolean canDrainFluidType(FluidStack fluid)
	{
		return false;
	}
	
	@Override
	public void update()
	{
		if(worldObj.isRemote)
		{
			return;
		}
		
		QuestInstance q = getQuest();
		IContainerTask t = getTask();
		
		if(worldObj.getTotalWorldTime()%10 == 0)
		{
			if(owner != null && q != null && t != null && owner != null && itemStack[0] != null)
			{
				if(t.canAcceptItem(owner, itemStack[0]))
				{
					Slot sIn = new Slot(this, 0, 0, 0);
					Slot sOut = new Slot(this, 1, 0, 0);
					t.submitItem(owner, sIn, sOut);
					
					if(((TaskBase)t).isComplete(owner))
					{
						q.UpdateClients();
						reset();
						worldObj.getMinecraftServer().getPlayerList().sendToAllNearExcept(null, pos.getX(), pos.getY(), pos.getZ(), 128, worldObj.provider.getDimension(), getUpdatePacket());
					} else
					{
						needsUpdate = true;
					}
				}
			}
			
			if(needsUpdate)
			{
				needsUpdate = false;
				
				if(q != null && !worldObj.isRemote)
				{
					q.UpdateClients();
				}
			} else if(t != null && ((TaskBase)t).isComplete(owner))
			{
				reset();
	    		worldObj.getMinecraftServer().getPlayerList().sendToAllNearExcept(null, pos.getX(), pos.getY(), pos.getZ(), 128, worldObj.provider.getDimension(), getUpdatePacket());
			}
		}
	}
	
	public void setupTask(UUID owner, QuestInstance quest, IContainerTask task)
	{
		if(owner == null || quest == null || task == null)
		{
			reset();
		}
		
		this.owner = owner;
		this.questID = quest.questID;
		this.taskID = quest.tasks.indexOf(task);
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
    	if(!worldObj.isRemote)
    	{
    		this.readFromNBT(data);
    		this.markDirty();
    		worldObj.getMinecraftServer().getPlayerList().sendToAllNearExcept(null, pos.getX(), pos.getY(), pos.getZ(), 128, worldObj.provider.getDimension(), getUpdatePacket());
    	} else
    	{
    		NBTTagCompound payload = new NBTTagCompound();
    		NBTTagCompound tileData = new NBTTagCompound();
    		this.writeToNBT(tileData);
    		payload.setTag("tile", tileData);
    		PacketAssembly.SendToServer(BQPacketType.EDIT_STATION.GetLocation(), payload);
    	}
    }
	
	@Override
	public void readFromNBT(NBTTagCompound tags)
	{
		super.readFromNBT(tags);
		
		itemStack[0] = ItemStack.loadItemStackFromNBT(tags.getCompoundTag("input"));
		itemStack[1] = ItemStack.loadItemStackFromNBT(tags.getCompoundTag("ouput"));
		
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
		tags.setTag("input", itemStack[0] != null? itemStack[0].writeToNBT(new NBTTagCompound()) : new NBTTagCompound());
		tags.setTag("output", itemStack[1] != null? itemStack[1].writeToNBT(new NBTTagCompound()) : new NBTTagCompound());
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
		ItemStack stack = itemStack[index];
		itemStack[index] = null;
		return stack;
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
		itemStack = new ItemStack[2];
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
			return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(this);
		}
		
        return super.getCapability(capability, facing);
    }
}
