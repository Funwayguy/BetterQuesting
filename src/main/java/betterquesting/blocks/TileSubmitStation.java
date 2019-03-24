package betterquesting.blocks;

import betterquesting.api.network.QuestingPacket;
import betterquesting.api.properties.NativeProps;
import betterquesting.api.questing.IQuest;
import betterquesting.api.questing.tasks.IFluidTask;
import betterquesting.api.questing.tasks.IItemTask;
import betterquesting.api.questing.tasks.ITask;
import betterquesting.api2.cache.CapabilityProviderQuestCache;
import betterquesting.api2.cache.QuestCache;
import betterquesting.core.BetterQuesting;
import betterquesting.network.PacketSender;
import betterquesting.network.PacketTypeNative;
import betterquesting.questing.QuestDatabase;
import betterquesting.storage.QuestSettings;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.UUID;

public class TileSubmitStation extends TileEntity implements IFluidHandler, ISidedInventory, ITickable, IFluidTankProperties
{
	private final IItemHandler itemHandler;
	private final IFluidHandler fluidHandler;
	private ItemStack[] itemStack = new ItemStack[2];
	private boolean needsUpdate = false;
	public UUID owner;
	public int questID;
	public int taskID;
	
	private IQuest qCached;
	
	@SuppressWarnings("WeakerAccess")
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
		    if(qCached == null) qCached = QuestDatabase.INSTANCE.getValue(questID);
			return qCached;
		}
	}
	
	@SuppressWarnings("WeakerAccess")
    public ITask getRawTask()
	{
		IQuest q = getQuest();
		
		if(q == null || taskID < 0)
		{
			return null;
		} else
		{
			return q.getTasks().getValue(taskID);
		}
	}
	
	@SuppressWarnings("WeakerAccess")
    public IItemTask getItemTask()
	{
		ITask t = getRawTask();
		return t == null? null : (t instanceof IItemTask? (IItemTask)t : null);
	}
	
	@SuppressWarnings("WeakerAccess")
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
		return ItemStackHelper.getAndSplit(itemStack, idx, amount);
	}

	@Override
	public void setInventorySlotContents(int idx, @Nonnull ItemStack stack)
	{
		if(idx < 0 || idx >= itemStack.length)
		{
			return;
		}
		
		itemStack[idx] = stack;
	}

	@Override
    @Nonnull
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
	public boolean isUseableByPlayer(@Nonnull EntityPlayer player)
	{
        return owner == null || player.getUniqueID().equals(owner);
        
    }

	@Override
	public void openInventory(@Nonnull EntityPlayer player)
	{
	}

	@Override
	public void closeInventory(@Nonnull EntityPlayer player)
	{
	}

	@Override
	public boolean isItemValidForSlot(int idx, @Nonnull ItemStack stack)
	{
		if(idx != 0)
		{
			return false;
		}
		
		IItemTask t = getItemTask();
		
		return t != null && itemStack[idx] == null && !t.isComplete(owner) && t.canAcceptItem(owner, getQuest(), stack);
	}
 
	@Override
	public int fill(FluidStack fluid, boolean doFill)
	{
		IFluidTask t = getFluidTask();
		
		if(!isSetup() || t == null) return 0;
		
		FluidStack remainder;
		int amount = fluid.amount;
		int consumed = 0;
		
		if(doFill)
		{
			remainder = t.submitFluid(owner, getQuest(), fluid);
		    consumed = remainder != null? amount - remainder.amount : amount;
		    
			if(t.isComplete(owner))
			{
				needsUpdate = true;
				reset();
				if(worldObj.getMinecraftServer() != null) worldObj.getMinecraftServer().getPlayerList().sendToAllNearExcept(null, pos.getX(), pos.getY(), pos.getZ(), 128, worldObj.provider.getDimension(), getUpdatePacket());
			} else
			{
				needsUpdate = consumed > 0;
			}
		}
		
		return consumed;
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
		
		return t != null && !t.isComplete(owner) && t.canAcceptFluid(owner, getQuest(), new FluidStack(fluid, 1));
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
		if(worldObj.isRemote || !isSetup() || QuestSettings.INSTANCE.getProperty(NativeProps.EDIT_MODE)) return;
		
		long wtt = worldObj.getTotalWorldTime();
		if(wtt%10 == 0 && owner != null)
		{
		    if(wtt%20 == 0) qCached = null; // Reset and lookup quest again once every second
            IQuest q = getQuest();
            IItemTask t = getItemTask();
            MinecraftServer server = worldObj.getMinecraftServer();
            EntityPlayerMP player = server == null ? null : server.getPlayerList().getPlayerByUUID(owner);
            QuestCache qc = player == null ? null : player.getCapability(CapabilityProviderQuestCache.CAP_QUEST_CACHE, null);
            
			if(q != null && t != null && itemStack[0] != null && itemStack[1] == null)
			{
				ItemStack inStack = itemStack[0].copy();
				ItemStack beforeStack = itemStack[0].copy();
				
				if(t.canAcceptItem(owner, getQuest(), inStack))
				{
					itemStack[0] = t.submitItem(owner, getQuest(), inStack); // Even if this returns an invalid item for submission it will be moved next pass
					
					if(t.isComplete(owner))
					{
						reset();
						needsUpdate = true;
						if(server != null) server.getPlayerList().sendToAllNearExcept(null, pos.getX(), pos.getY(), pos.getZ(), 128, worldObj.provider.getDimension(), getUpdatePacket());
					} else
					{
						if(itemStack[0] == null || !itemStack[0].equals(beforeStack)) needsUpdate = true;
					}
				} else
				{
					itemStack[1] = inStack;
					itemStack[0] = null;
				}
			}
			
			if(needsUpdate)
			{
				needsUpdate = false;
				
				if(q != null && qc != null)
				{
				    qc.markQuestDirty(questID); // Let the cache take care of syncing
				}
			}
			
			if(t != null && t.isComplete(owner))
			{
				reset();
				worldObj.getMinecraftServer().getPlayerList().sendToAllNearExcept(null, pos.getX(), pos.getY(), pos.getZ(), 128, worldObj.provider.getDimension(), getUpdatePacket());
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
		
		this.questID = QuestDatabase.INSTANCE.getID(quest);
		this.qCached = quest;
		this.taskID = quest.getTasks().getID(task);
		
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
		return owner != null && questID >= 0 && taskID >= 0;
	}
	
	public void reset()
	{
		owner = null;
		questID = -1;
		taskID = -1;
		qCached = null;
		this.markDirty();
	}

    /**
     * Overridden in a sign to provide the text.
     */
    @Nonnull
	@Override
    public SPacketUpdateTileEntity getUpdatePacket()
    {
        return new SPacketUpdateTileEntity(pos, 0, this.writeToNBT(new NBTTagCompound()));
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
    		if(data != null) this.readFromNBT(data); // Note: The handler has already read out the "tile" subtag in advance
    		this.markDirty();
    		if(worldObj.getMinecraftServer() != null) worldObj.getMinecraftServer().getPlayerList().sendToAllNearExcept(null, pos.getX(), pos.getY(), pos.getZ(), 128, worldObj.provider.getDimension(), getUpdatePacket());
    	} else
    	{
    		NBTTagCompound payload = new NBTTagCompound();
    		payload.setTag("tile", this.writeToNBT(new NBTTagCompound()));
    		PacketSender.INSTANCE.sendToServer(new QuestingPacket(PacketTypeNative.EDIT_STATION.GetLocation(), payload));
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
		
		if(!isSetup()) // All data must be present for this to run correctly
		{
			this.reset();
		}
	}
	
	@Override
    @Nonnull
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

	private static final int[] slotsForFace = new int[]{0, 1};
 
	@Override
    @Nonnull
	public int[] getSlotsForFace(@Nullable EnumFacing side)
	{
		return slotsForFace;
	}

	@Override
	public boolean canInsertItem(int slot, @Nonnull ItemStack stack, @Nullable EnumFacing side)
	{
		return isItemValidForSlot(slot, stack);
	}

	@Override
	public boolean canExtractItem(int slot, @Nonnull ItemStack stack, @Nullable EnumFacing side)
	{
		return slot == 1;
	}

	@Override
    @Nonnull
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
        Arrays.fill(itemStack, null);
	}

	@Override
    @Nonnull
	public ITextComponent getDisplayName()
	{
		return new TextComponentString(BetterQuesting.submitStation.getLocalizedName());
	}
	
	@Override
    public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing)
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
	
    @Nonnull
	@Override
    public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing)
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
}
