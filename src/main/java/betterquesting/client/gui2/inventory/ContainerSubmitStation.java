package betterquesting.client.gui2.inventory;

import betterquesting.blocks.TileSubmitStation;
import betterquesting.misc.Util;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.SlotItemHandler;

import javax.annotation.Nonnull;

public class ContainerSubmitStation extends Container {
  public final TileSubmitStation tile;
  private final InventoryPlayer player;

  public ContainerSubmitStation(InventoryPlayer player, TileSubmitStation tile) {
    this.tile = tile;
    this.player = player;

    IItemHandler handler = tile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
    addSlotToContainer(new SlotItemHandler(handler, 0, 0, 0));
    addSlotToContainer(new SlotItemHandler(handler, 1, 0, 0));

    for (int i = 0; i < 3; ++i) {
      for (int j = 0; j < 9; ++j) {
        this.addSlotToContainer(new Slot(player, j + i * 9 + 9, j * 18, i * 18));
      }
    }

    for (int i = 0; i < 9; ++i) {
      this.addSlotToContainer(new Slot(player, i, i * 18, 58));
    }
  }

  @Override
  public void detectAndSendChanges() {
    if (player.player instanceof EntityPlayerMP) {
      boolean old = ((EntityPlayerMP) player.player).isChangingQuantityOnly;
      ((EntityPlayerMP) player.player).isChangingQuantityOnly = false;
      super.detectAndSendChanges();
      ((EntityPlayerMP) player.player).isChangingQuantityOnly = old;
    } else {
      super.detectAndSendChanges();
    }
  }

  public void moveInventorySlots(int x, int y) {
    int idx = 2;

    for (int i = 0; i < 3; ++i) {
      for (int j = 0; j < 9; ++j) {
        Slot s = inventorySlots.get(idx);
        s.xPos = j * 18 + x;
        s.yPos = i * 18 + y;
        idx++;
      }
    }

    for (int i = 0; i < 9; ++i) {
      Slot s = inventorySlots.get(idx);
      s.xPos = i * 18 + x;
      s.yPos = 58 + y;
      idx++;
    }
  }

  @Override
  protected boolean mergeItemStack(ItemStack stack, int startIndex, int endIndex, boolean reverseDirection) {
    boolean flag = false;
    Iterable<Integer> range = Util.closedOpenRange(startIndex, endIndex, reverseDirection);

    if (stack.isStackable()) {
      for (int i : range) {
        Slot slot = this.inventorySlots.get(i);
        ItemStack slotStack = slot.getStack();

        if (ItemHandlerHelper.canItemStacksStack(stack, slotStack)) {
          int total = slotStack.getCount() + stack.getCount();
          int maxSize = Math.min(stack.getMaxStackSize(), slot.getItemStackLimit(stack));
          if (total <= maxSize) {
            stack.setCount(0);
            slotStack.setCount(total);
            slot.onSlotChanged();
            flag = true;
          } else if (slotStack.getCount() < maxSize) {
            stack.shrink(maxSize - slotStack.getCount());
            slotStack.setCount(maxSize);
            slot.onSlotChanged();
            flag = true;
          }
        }
      }
    }

    for (int i : range) {
      Slot slot = this.inventorySlots.get(i);
      ItemStack slotStack = slot.getStack();

      if (slotStack.isEmpty() && slot.isItemValid(stack)) {
        int maxSize = Math.min(slot.getItemStackLimit(stack), stack.getMaxStackSize());
        slot.putStack(stack.splitStack(Math.min(maxSize, stack.getCount())));
        slot.onSlotChanged();
        flag = true;
        break;
      }
    }

    return flag;
  }

  /**
   * Called when a player shift-clicks on a slot. You must override this or you will crash when someone does that.
   */
  @Nonnull
  @Override
  public ItemStack transferStackInSlot(@Nonnull EntityPlayer player, int idx) {
    if (idx < 0) {
      return ItemStack.EMPTY;
    }

    ItemStack itemstack = ItemStack.EMPTY;
    Slot slot = this.inventorySlots.get(idx);

    if (slot != null && slot.getHasStack()) {
      ItemStack itemstack1 = slot.getStack();
      itemstack = itemstack1.copy();

      if (idx == 0) {
        if (!this.mergeItemStack(itemstack1, 1, 37, true)) {
          return ItemStack.EMPTY;
        }

        slot.onSlotChange(itemstack1, itemstack);
      } else if (slot.isItemValid(itemstack1)) {
        if (!this.mergeItemStack(itemstack1, 0, 1, false)) {
          return ItemStack.EMPTY;
        }
      } else if (idx < 28) {
        if (!this.mergeItemStack(itemstack1, 28, 37, false)) {
          return ItemStack.EMPTY;
        }
      } else if (idx < 37) {
        if (!this.mergeItemStack(itemstack1, 1, 28, false)) {
          return ItemStack.EMPTY;
        }
      } else if (!this.mergeItemStack(itemstack1, 1, 37, false)) {
        return ItemStack.EMPTY;
      }

      if (itemstack1.isEmpty()) {
        slot.putStack(ItemStack.EMPTY);
      } else {
        slot.onSlotChanged();
      }

      if (itemstack1.getCount() == itemstack.getCount()) {
        return ItemStack.EMPTY;
      }

      slot.onTake(player, itemstack1);
    }

    return itemstack;
  }

  public void moveSubmitSlot(int x, int y) {
    Slot s = inventorySlots.get(0);
    s.xPos = x;
    s.yPos = y;
  }

  public void moveReturnSlot(int x, int y) {
    Slot s = inventorySlots.get(1);
    s.xPos = x;
    s.yPos = y;
  }

  @Override
  public boolean canInteractWith(@Nonnull EntityPlayer player) {
    return tile.isUsableByPlayer(player);
  }
}
