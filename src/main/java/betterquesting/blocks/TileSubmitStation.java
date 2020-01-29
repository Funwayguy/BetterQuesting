package betterquesting.blocks;

import betterquesting.api.network.QuestingPacket;
import betterquesting.api.properties.NativeProps;
import betterquesting.api.questing.IQuest;
import betterquesting.api.questing.tasks.IFluidTask;
import betterquesting.api.questing.tasks.IItemTask;
import betterquesting.api.questing.tasks.ITask;
import betterquesting.api2.cache.QuestCache;
import betterquesting.core.BetterQuesting;
import betterquesting.network.PacketSender;
import betterquesting.network.PacketTypeNative;
import betterquesting.questing.QuestDatabase;
import betterquesting.storage.QuestSettings;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.UUID;

public class TileSubmitStation extends TileEntity implements IFluidHandler, ISidedInventory
{
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
	public ItemStack getStackInSlotOnClosing(int slot)
	{
		return null;
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
    
    @Nonnull
	@Override
	public String getInventoryName()
	{
		return BetterQuesting.submitStation.getLocalizedName();
	}

	@Override
	public boolean hasCustomInventoryName()
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
        return owner == null || player.getUniqueID().equals(owner);
        
    }

	@Override
	public void openInventory()
	{
	}

	@Override
	public void closeInventory()
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
		
		return t != null && itemStack[idx] == null && !t.isComplete(owner) && t.canAcceptItem(owner, getQuest(), stack);
	}
 
	@Override
	public int fill(ForgeDirection from, FluidStack fluid, boolean doFill)
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
				MinecraftServer server = MinecraftServer.getServer();
				if(server != null) server.getConfigurationManager().sendToAllNearExcept(null, xCoord, yCoord, zCoord, 128, worldObj.provider.dimensionId, getDescriptionPacket());
			} else
			{
				needsUpdate = consumed > 0;
			}
		}
		
		return consumed;
	}

	@Override
	public FluidStack drain(ForgeDirection from, FluidStack resource, boolean doDrain)
	{
		return null;
	}

	@Override
	public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain)
	{
		return null;
	}
	
	@Override
	public boolean canFill(ForgeDirection from, Fluid fluid)
	{
		IFluidTask t = getFluidTask();
		
		return t != null && !t.isComplete(owner) && t.canAcceptFluid(owner, getQuest(), new FluidStack(fluid, 1));
	}
	
	@Override
	public boolean canDrain(ForgeDirection from, Fluid fluid)
	{
		return false;
	}

	@Override
	public FluidTankInfo[] getTankInfo(ForgeDirection from)
	{
		return new FluidTankInfo[0];
	}
	
	@Override
	public void updateEntity()
	{
		if(worldObj.isRemote || !isSetup() || QuestSettings.INSTANCE.getProperty(NativeProps.EDIT_MODE)) return;
		
		long wtt = worldObj.getTotalWorldTime();
		if(wtt%10 == 0 && owner != null)
		{
		    if(wtt%20 == 0) qCached = null; // Reset and lookup quest again once every second
            IQuest q = getQuest();
            IItemTask t = getItemTask();
            MinecraftServer server = MinecraftServer.getServer();
            EntityPlayerMP player = getPlayerByUUID(owner);
            QuestCache qc = player == null ? null : (QuestCache)player.getExtendedProperties(QuestCache.LOC_QUEST_CACHE.toString());
            
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
						if(server != null) server.getConfigurationManager().sendToAllNearExcept(null, xCoord, yCoord, zCoord, 128, worldObj.provider.dimensionId, getDescriptionPacket());
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
				MinecraftServer.getServer().getConfigurationManager().sendToAllNearExcept(null, xCoord, yCoord, zCoord, 128, worldObj.provider.dimensionId, getDescriptionPacket());
			}
		}
	}
	
	private EntityPlayerMP getPlayerByUUID(UUID uuid)
    {
        MinecraftServer server = MinecraftServer.getServer();
        if(server == null) return null;
        
        for(EntityPlayerMP player : (List<EntityPlayerMP>)server.getConfigurationManager().playerEntityList)
        {
            if(player.getGameProfile().getId().equals(uuid)) return player;
        }
        
        return null;
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
    public Packet getDescriptionPacket()
    {
        NBTTagCompound nbtTagCompound = new NBTTagCompound();
        this.writeToNBT(nbtTagCompound);
        return new S35PacketUpdateTileEntity(xCoord, yCoord, zCoord, 0, nbtTagCompound);
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
    public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity pkt)
    {
    	this.readFromNBT(pkt.func_148857_g());
    }
    
    /**
     * Ignores parameter on client side (uses own data instead)
     */
    public void SyncTile(NBTTagCompound data)
    {
    	if(!worldObj.isRemote)
    	{
    		if(data != null) this.readFromNBT(data); // Note: The handler has already read out the "tile" subtag in advance
		try
			{
				this.owner = UUID.fromString(data.getString("owner"));
			}
			catch (Exception e)
			{
				this.reset();
				return;
			}

			this.questID = data.hasKey("questID") ? data.getInteger("questID") : -1;
			this.taskID = data.hasKey("task") ? data.getInteger("task") : -1;
			if (this.isSetup())
			{
				BetterQuesting.logger.log(Level.ERROR, "One or more tags were missing!", new Exception());
				this.reset();
			}
    		this.markDirty();
    		MinecraftServer server = MinecraftServer.getServer();
    		if(server != null) server.getConfigurationManager().sendToAllNearExcept(null, xCoord, yCoord, zCoord, 128, worldObj.provider.dimensionId, getDescriptionPacket());
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
		
		itemStack[0] = ItemStack.loadItemStackFromNBT(tags.getCompoundTag("input"));
		itemStack[1] = ItemStack.loadItemStackFromNBT(tags.getCompoundTag("output"));
		
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
	public void writeToNBT(NBTTagCompound tags)
	{
		super.writeToNBT(tags);
		tags.setString("owner", owner != null? owner.toString() : "");
		tags.setInteger("questID", questID);
		tags.setInteger("task", taskID);
		tags.setTag("input", itemStack[0] != null? itemStack[0].writeToNBT(new NBTTagCompound()) : new NBTTagCompound());
		tags.setTag("output", itemStack[1] != null? itemStack[1].writeToNBT(new NBTTagCompound()) : new NBTTagCompound());
	}

	private static final int[] slotsForFace = new int[]{0, 1};
 
	@Override
    @Nonnull
	public int[] getAccessibleSlotsFromSide(int side)
	{
		return slotsForFace;
	}

	@Override
	public boolean canInsertItem(int slot, ItemStack stack, int side)
	{
		return isItemValidForSlot(slot, stack);
	}

	@Override
	public boolean canExtractItem(int slot, ItemStack stack, int side)
	{
		return slot == 1;
	}
}
