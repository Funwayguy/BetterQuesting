package betterquesting.blocks;

import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandlerModifiable;

import javax.annotation.Nonnull;

public class SSItemHandler implements IItemHandlerModifiable {
    private final TileSubmitStation tile;

    public SSItemHandler(TileSubmitStation tile) {
        this.tile = tile;
    }

    @Override
    public int getSlots() {
        return tile.getSizeInventory();
    }

    @Nonnull
    @Override
    public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
        if (stack.isEmpty() || !tile.isItemValidForSlot(slot, stack)) {
            return stack;
        }

        // Existing stack
        ItemStack ts1 = getStackInSlot(slot);

        if (!ts1.isEmpty() && !stack.isItemEqual(ts1)) {
            return stack;
        }

        int inMax = Math.min(stack.getCount(), stack.getMaxStackSize() - ts1.getCount());
        // Input stack
        ItemStack ts2 = stack.copy();
        ts2.setCount(inMax);

        if (!simulate) {
            if (ts1.isEmpty()) {
                ts1 = ts2;
            } else {
                ts1.grow(ts2.getCount());
            }

            tile.setInventorySlotContents(slot, ts1);
        }

        if (stack.getCount() > inMax) {
            // Left over stack
            ItemStack ts3 = stack.copy();
            ts3.setCount(stack.getCount() - inMax);
            return ts3;
        }

        return ItemStack.EMPTY;
    }

    @Nonnull
    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (slot != 1 || amount <= 0) {
            return ItemStack.EMPTY;
        }

        if (!simulate) {
            return tile.decrStackSize(slot, amount);
        }

        ItemStack stack = getStackInSlot(slot);

        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }

        int outMax = Math.min(stack.getCount(), amount);

        ItemStack ts1 = stack.copy();
        ts1.setCount(outMax);

        return ts1;
    }

    @Override
    public void setStackInSlot(int slot, @Nonnull ItemStack stack) {
        tile.setInventorySlotContents(slot, stack);
    }

    @Nonnull
    @Override
    public ItemStack getStackInSlot(int idx) {
        return tile.getStackInSlot(idx);
    }

    @Override
    public int getSlotLimit(int slot) {
        return 64;
    }
}
