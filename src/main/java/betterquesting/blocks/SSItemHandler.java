package betterquesting.blocks;

import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandlerModifiable;

public class SSItemHandler implements IItemHandlerModifiable
{
	private final TileSubmitStation tile;
	
	public SSItemHandler(TileSubmitStation tile)
	{
		this.tile = tile;
	}
	
	@Override
	public int getSlots()
	{
		return tile.getSizeInventory();
	}

	@Override
	public ItemStack insertItem(int slot, ItemStack stack, boolean simulate)
	{
		if(stack == null)
		{
			return null;
		} else if(!tile.isItemValidForSlot(slot, stack))
		{
			return stack;
		}
		
		// Existing stack
		ItemStack ts1 = getStackInSlot(slot);
		
		if(ts1 != null && !stack.isItemEqual(ts1))
		{
			return stack;
		}
		
		int inMax = Math.min(stack.func_190916_E(), stack.getMaxStackSize() - (ts1 == null? 0 : ts1.func_190916_E()));
		// Input stack
		ItemStack ts2 = stack.copy();
		ts2.func_190920_e(inMax);
		
		if(!simulate)
		{
			if(ts1 == null)
			{
				ts1 = ts2;
			} else
			{
				ts1.func_190917_f(ts2.func_190916_E());
			}
			
			tile.setInventorySlotContents(slot, ts1);
		}
		
		if(stack.func_190916_E() > inMax)
		{
			// Left over stack
			ItemStack ts3 = stack.copy();
			ts3.func_190920_e(stack.func_190916_E() - inMax);
			return ts3;
		}
		
		return null;
	}

	@Override
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
		
		int outMax = Math.min(stack.func_190916_E(), amount);
		
		ItemStack ts1 = stack.copy();
		ts1.func_190920_e(outMax);
		
		return ts1;
	}

	@Override
	public void setStackInSlot(int slot, ItemStack stack)
	{
		tile.setInventorySlotContents(slot, stack);
	}

	@Override
	public ItemStack getStackInSlot(int idx)
	{
		return tile.getStackInSlot(idx);
	}
}
