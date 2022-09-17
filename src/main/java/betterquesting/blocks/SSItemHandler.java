package betterquesting.blocks;

import net.minecraft.item.ItemStack;

public class SSItemHandler
{
	private final TileSubmitStation tile;
	
	public SSItemHandler(TileSubmitStation tile)
	{
		this.tile = tile;
	}
	
	public int getSlots()
	{
		return tile.getSizeInventory();
	}
 
	public ItemStack insertItem(int slot, ItemStack stack, boolean simulate)
	{
		if(stack == null || !tile.isItemValidForSlot(slot, stack))
		{
			return stack;
		}
		
		// Existing stack
		ItemStack ts1 = getStackInSlot(slot);
		
		if(ts1 != null && !stack.isItemEqual(ts1))
		{
			return stack;
		}
		
		int inMax = Math.min(stack.stackSize, stack.getMaxStackSize() - (ts1 == null ? 0 : ts1.stackSize));
		// Input stack
		ItemStack ts2 = stack.copy();
		ts2.stackSize = inMax;
		
		if(!simulate)
		{
		    if(ts1 == null)
            {
                ts1 = ts2;
            } else
            {
                ts1.stackSize += ts2.stackSize;
            }
			tile.setInventorySlotContents(slot, ts1);
		}
		
		if(stack.stackSize > inMax)
		{
			// Left over stack
			ItemStack ts3 = stack.copy();
			ts3.stackSize = stack.stackSize - inMax;
			return ts3;
		}
		
		return null;
	}
 
	public ItemStack extractItem(int slot, int amount, boolean simulate)
	{
		if(slot != 1 || amount <= 0)
		{
			return null;
		}
		
		if(!simulate)
		{
			return tile.decrStackSize(slot, amount);
		}
		
		ItemStack stack = getStackInSlot(slot);
		
		if(stack == null)
		{
			return null;
		}
		
		int outMax = Math.min(stack.stackSize, amount);
		
		ItemStack ts1 = stack.copy();
		ts1.stackSize = outMax;
		
		return ts1;
	}
 
	public void setStackInSlot(int slot, ItemStack stack)
	{
		tile.setInventorySlotContents(slot, stack);
	}
 
	public ItemStack getStackInSlot(int idx)
	{
		return tile.getStackInSlot(idx);
	}
}
