package betterquesting.client.gui2.inventory;

import betterquesting.blocks.TileSubmitStation;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class ContainerSubmitStation extends Container {
    private TileSubmitStation tile;

    public ContainerSubmitStation(InventoryPlayer inventory, TileSubmitStation tile) {
        this.tile = tile;

        this.addSlotToContainer(new Slot(tile, 0, 0, 0) {
            @Override
            public boolean isItemValid(ItemStack stack) {
                return inventory.isItemValidForSlot(0, stack);
            }
        });

        this.addSlotToContainer(new Slot(tile, 1, 0, 0) {
            @Override
            public boolean isItemValid(ItemStack stack) {
                return false;
            }
        });

        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                this.addSlotToContainer(new Slot(inventory, j + i * 9 + 9, j * 18, i * 18));
            }
        }

        for (int i = 0; i < 9; ++i) {
            this.addSlotToContainer(new Slot(inventory, i, i * 18, 58));
        }
    }

    public void moveInventorySlots(int x, int y) {
        int idx = 2;

        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                Slot s = (Slot) inventorySlots.get(idx);
                s.xDisplayPosition = j * 18 + x;
                s.yDisplayPosition = i * 18 + y;
                idx++;
            }
        }

        for (int i = 0; i < 9; ++i) {
            Slot s = (Slot) inventorySlots.get(idx);
            s.xDisplayPosition = i * 18 + x;
            s.yDisplayPosition = 58 + y;
            idx++;
        }
    }

    /**
     * Called when a player shift-clicks on a slot. You must override this or you will crash when someone does that.
     */
    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int idx) {
        if (idx < 0) return null;

        ItemStack itemstack = null;
        Slot slot = (Slot) this.inventorySlots.get(idx);

        if (slot != null && slot.getHasStack()) {
            ItemStack itemstack1 = slot.getStack();
            itemstack = itemstack1.copy();

            if (idx == 0) {
                if (!this.mergeItemStack(itemstack1, 1, 37, true)) {
                    return null;
                }

                slot.onSlotChange(itemstack1, itemstack);
            } else if (slot.isItemValid(itemstack1)) {
                if (!this.mergeItemStack(itemstack1, 0, 1, false)) {
                    return null;
                }
            } else if (idx < 28) {
                if (!this.mergeItemStack(itemstack1, 28, 37, false)) {
                    return null;
                }
            } else if (idx < 37) {
                if (!this.mergeItemStack(itemstack1, 1, 28, false)) {
                    return null;
                }
            } else if (!this.mergeItemStack(itemstack1, 1, 37, false)) {
                return null;
            }

            if (itemstack1.stackSize == 0) {
                slot.putStack(null);
            } else {
                slot.onSlotChanged();
            }

            if (itemstack1.stackSize == itemstack.stackSize) {
                return null;
            }

            slot.onPickupFromSlot(player, itemstack1);
        }

        return itemstack;
    }

    public void moveSubmitSlot(int x, int y) {
        Slot s = (Slot) inventorySlots.get(0);
        s.xDisplayPosition = x;
        s.yDisplayPosition = y;
    }

    public void moveReturnSlot(int x, int y) {
        Slot s = (Slot) inventorySlots.get(1);
        s.xDisplayPosition = x;
        s.yDisplayPosition = y;
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return tile.isUseableByPlayer(player);
    }
}
