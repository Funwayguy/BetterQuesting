package betterquesting.blocks;

import java.util.UUID;
import net.minecraft.entity.player.EntityPlayer;
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
import org.apache.logging.log4j.Level;
import betterquesting.api.ExpansionAPI;
import betterquesting.api.network.PacketTypeNative;
import betterquesting.api.network.PreparedPayload;
import betterquesting.api.quests.IQuest;
import betterquesting.api.quests.tasks.IFluidTask;
import betterquesting.api.quests.tasks.IItemTask;
import betterquesting.api.quests.tasks.ITask;
import betterquesting.core.BetterQuesting;
import betterquesting.database.QuestDatabase;
import betterquesting.network.PacketSender;

public class TileSubmitStation extends TileEntity implements IFluidHandler, ISidedInventory
{
	ItemStack[] itemStack = new ItemStack[2];
	boolean needsUpdate = false;
	public UUID owner;
	public int questID;
	public int taskID;
	
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

	@Override
	public String getInventoryName()
	{
		return "Object Submission Station";
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
		if(owner == null || ExpansionAPI.getAPI().getNameCache().getQuestingID(player).equals(owner))
		{
			return true;
		}
		
		return false;
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
		
		return t != null && itemStack[1] == null && !((ITask)t).isComplete(owner) && t.canAcceptItem(owner, stack);
	}

	@Override
	public int fill(ForgeDirection from, FluidStack fluid, boolean doFill)
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
	    		MinecraftServer.getServer().getConfigurationManager().sendToAllNear(xCoord, yCoord, zCoord, 128, worldObj.provider.dimensionId, getDescriptionPacket());
			} else
			{
				needsUpdate = true;
			}
		}
		
		return remainder != null? amount - remainder.amount : amount;
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
		
		return t != null && !((ITask)t).isComplete(owner) && t.canAcceptFluid(owner, new FluidStack(fluid, 1));
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
		if(worldObj.isRemote)
		{
			return;
		}
		
		IQuest q = getQuest();
		IItemTask t = getItemTask();
		
		if(worldObj.getTotalWorldTime()%10 == 0)
		{
			if(owner != null && q != null && t != null && owner != null && itemStack[0] != null && itemStack[1] == null)
			{
				ItemStack inStack = itemStack[0].copy();
				
				if(t.canAcceptItem(owner, inStack))
				{
					itemStack[0] = t.submitItem(owner, inStack); // Even if this returns an invalid item for submission it will be moved next pass
					
					if(((ITask)t).isComplete(owner))
					{
						PacketSender.INSTANCE.sendToAll(q.getSyncPacket());
						reset();
			    		MinecraftServer.getServer().getConfigurationManager().sendToAllNear(xCoord, yCoord, zCoord, 128, worldObj.provider.dimensionId, getDescriptionPacket());
					} else
					{
						needsUpdate = true;
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
				
				if(q != null && !worldObj.isRemote)
				{
					PacketSender.INSTANCE.sendToAll(q.getSyncPacket());
				}
			} else if(t != null && ((ITask)t).isComplete(owner))
			{
				reset();
	    		MinecraftServer.getServer().getConfigurationManager().sendToAllNear(xCoord, yCoord, zCoord, 128, worldObj.provider.dimensionId, getDescriptionPacket());
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
    public Packet getDescriptionPacket()
    {
        NBTTagCompound nbttagcompound = new NBTTagCompound();
        this.writeToNBT(nbttagcompound);
        return new S35PacketUpdateTileEntity(this.xCoord, this.yCoord, this.zCoord, 0, nbttagcompound);
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
    		this.readFromNBT(data);
    		this.markDirty();
    		MinecraftServer.getServer().getConfigurationManager().sendToAllNear(xCoord, yCoord, zCoord, 128, worldObj.provider.dimensionId, getDescriptionPacket());
    	} else
    	{
    		NBTTagCompound payload = new NBTTagCompound();
    		NBTTagCompound tileData = new NBTTagCompound();
    		this.writeToNBT(tileData);
    		payload.setTag("tile", tileData);
			PacketSender.INSTANCE.sendToServer(new PreparedPayload(PacketTypeNative.EDIT_STATION.GetLocation(), payload));
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
	public void writeToNBT(NBTTagCompound tags)
	{
		super.writeToNBT(tags);
		tags.setString("owner", owner != null? owner.toString() : "");
		tags.setInteger("questID", questID);
		tags.setInteger("task", taskID);
		tags.setTag("input", itemStack[0] != null? itemStack[0].writeToNBT(new NBTTagCompound()) : new NBTTagCompound());
		tags.setTag("output", itemStack[1] != null? itemStack[1].writeToNBT(new NBTTagCompound()) : new NBTTagCompound());
	}

	@Override
	public int[] getAccessibleSlotsFromSide(int side)
	{
		return new int[]{0,1};
	}

	@Override
	public boolean canInsertItem(int slot, ItemStack stack, int side)
	{
		return slot == 0 && isItemValidForSlot(slot, stack);
	}

	@Override
	public boolean canExtractItem(int slot, ItemStack stack, int side)
	{
		return slot == 1;
	}
}
