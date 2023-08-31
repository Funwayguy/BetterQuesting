package betterquesting.blocks;

import betterquesting.api.questing.IQuest;
import betterquesting.api.questing.tasks.IEnergyTask;
import betterquesting.api.questing.tasks.IFluidTask;
import betterquesting.api.questing.tasks.IItemTask;
import betterquesting.api.questing.tasks.ITask;
import betterquesting.api2.storage.DBEntry;
import betterquesting.core.BetterQuesting;
import betterquesting.questing.QuestDatabase;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Tuple;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;

public class TileSubmitStation extends TileEntity implements IInventory {
  private final ItemHandler itemHandler = new ItemHandler();
  private final FluidHandler fluidHandler = new FluidHandler();
  private final EnergyHandler energyStorage = new EnergyHandler();
  public UUID owner = null;
  public int questID = -1;
  public int taskID = -1;

  @Nullable
  public <T extends ITask> Tuple<DBEntry<IQuest>, T> getQuestTask(Class<T> taskClazz) {
    if (questID < 0) {
      return null;
    }
    IQuest quest = QuestDatabase.INSTANCE.getValue(questID);
    if (quest == null) {
      reset();
      return null;
    }
    ITask task = quest.getTasks().getValue(taskID);
    if (task == null) {
      reset();
      return null;
    }
    if (!taskClazz.isInstance(task)) {
      return null;
    }
    return new Tuple<>(new DBEntry<>(questID, quest), taskClazz.cast(task));
  }

  private void syncBlockState() {
    if (world.getMinecraftServer() != null) {
      IBlockState state = blockType.getStateFromMeta(getBlockMetadata());
      world.notifyBlockUpdate(pos, state, state, 2); //Per Forge documentation this is the proper way to cause block update.
    }
  }

  public void setupTask(UUID owner, int questID, int taskID) {
    if (owner == null || questID < 0 || taskID < 0) {
      reset();
      return;
    }
    this.questID = questID;
    this.taskID = taskID;
    this.owner = owner;
    this.markDirty();
  }

  public boolean isSetup() {
    return owner != null && questID >= 0 && taskID >= 0;
  }

  public void reset() {
    owner = null;
    questID = -1;
    taskID = -1;
    this.markDirty();
  }

  @Nonnull
  @Override
  public SPacketUpdateTileEntity getUpdatePacket() {
    return new SPacketUpdateTileEntity(pos, 0, writeToNBT(new NBTTagCompound()));
  }

  @Nonnull
  @Override
  public NBTTagCompound getUpdateTag() {
    return writeToNBT(new NBTTagCompound());
  }

  @Override
  public void onDataPacket(@Nonnull NetworkManager net, SPacketUpdateTileEntity pkt) {
    readFromNBT(pkt.getNbtCompound());
  }

  @Override
  public void readFromNBT(@Nonnull NBTTagCompound tags) {
    super.readFromNBT(tags);

    try {
      owner = UUID.fromString(tags.getString("owner"));
    } catch (Exception e) {
      this.reset();
      return;
    }

    questID = tags.hasKey("questID") ? tags.getInteger("questID") : -1;
    taskID = tags.hasKey("task") ? tags.getInteger("task") : -1;

    itemHandler.fromTag(tags);

    // All data must be present for this to run correctly
    if (!isSetup()) {
      this.reset();
    }
  }

  @Override
  @Nonnull
  public NBTTagCompound writeToNBT(@Nonnull NBTTagCompound tags) {
    super.writeToNBT(tags);
    tags.setString("owner", owner != null ? owner.toString() : "");
    tags.setInteger("questID", questID);
    tags.setInteger("task", taskID);
    itemHandler.toTag(tags);
    return tags;
  }

  @Nonnull
  @Override
  public String getName() {
    return "Submit Station";
  }

  @Override
  public boolean hasCustomName() {
    return false;
  }

  @Override
  @Nonnull
  public ITextComponent getDisplayName() {
    return new TextComponentString(BetterQuesting.submitStation.getLocalizedName());
  }

  @Override
  public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
    return capability == CapabilityEnergy.ENERGY || capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY ||
           capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY || super.hasCapability(capability, facing);

  }

  @Override
  @Nullable
  public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
    if (capability == CapabilityEnergy.ENERGY) {
      return CapabilityEnergy.ENERGY.cast(energyStorage);
    } else if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
      return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(itemHandler);
    } else if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
      return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(fluidHandler);
    }

    return super.getCapability(capability, facing);
  }

  @Override
  public int getSizeInventory() {
    return 2;
  }

  @Override
  public boolean isEmpty() {
    return !itemHandler.outputStack.isEmpty();
  }

  @Nonnull
  @Override
  public ItemStack getStackInSlot(int index) {
    return itemHandler.getStackInSlot(index);
  }

  @Nonnull
  @Override
  public ItemStack decrStackSize(int index, int count) {
    return itemHandler.extractItem(index, count, false);
  }

  @Nonnull
  @Override
  public ItemStack removeStackFromSlot(int index) {
    ItemStack oldStack = itemHandler.getStackInSlot(index);
    itemHandler.setStackInSlot(index, ItemStack.EMPTY);
    return oldStack;
  }

  @Override
  public void setInventorySlotContents(int index, @Nonnull ItemStack stack) {
    itemHandler.setStackInSlot(index, stack);
  }

  @Override
  public int getInventoryStackLimit() {
    return 64;
  }

  @Override
  public boolean isUsableByPlayer(@Nonnull EntityPlayer player) {
    return (owner == null || player.getUniqueID().equals(owner)) && player.getDistanceSq(this.pos) < 256;
  }

  @Override
  public void openInventory(@Nonnull EntityPlayer player) { }

  @Override
  public void closeInventory(@Nonnull EntityPlayer player) { }

  @Override
  public boolean isItemValidForSlot(int index, @Nonnull ItemStack stack) {
    return itemHandler.isItemValid(index, stack);
  }

  @Override
  public int getField(int id) {
    return 0;
  }

  @Override
  public void setField(int id, int value) { }

  @Override
  public int getFieldCount() {
    return 0;
  }

  @Override
  public void clear() {
    itemHandler.setStackInSlot(1, ItemStack.EMPTY);
  }

  private class FluidHandler implements IFluidHandler {
    private final IFluidTankProperties[] properties = { new IFluidTankProperties() {
      @Nullable
      @Override
      public FluidStack getContents() {
        return null;
      }

      @Override
      public int getCapacity() {
        return Integer.MAX_VALUE;
      }

      @Override
      public boolean canFill() {
        Tuple<DBEntry<IQuest>, IFluidTask> questTask = getQuestTask(IFluidTask.class);
        return questTask != null && !questTask.getSecond().isComplete(owner);
      }

      @Override
      public boolean canDrain() {
        return false;
      }

      @Override
      public boolean canFillFluidType(FluidStack fluidStack) {
        Tuple<DBEntry<IQuest>, IFluidTask> questTask = getQuestTask(IFluidTask.class);
        return questTask != null && questTask.getSecond().canAcceptFluid(owner, questTask.getFirst(), fluidStack);
      }

      @Override
      public boolean canDrainFluidType(FluidStack fluidStack) {
        return false;
      }
    } };

    @Override
    public IFluidTankProperties[] getTankProperties() {
      return properties;
    }

    @Override
    public int fill(FluidStack fluid, boolean doFill) {
      Tuple<DBEntry<IQuest>, IFluidTask> questTask = getQuestTask(IFluidTask.class);
      if (questTask == null) {
        return 0;
      }
      doFill &= !world.isRemote;
      DBEntry<IQuest> quest = questTask.getFirst();
      IFluidTask task = questTask.getSecond();
      fluid = fluid.copy();
      FluidStack remainder = task.submitFluid(owner, quest, fluid, doFill);
      int accepted = remainder == null ? fluid.amount : fluid.amount - remainder.amount;
      if (doFill) {
        if (task.isComplete(owner)) {
          reset();
          syncBlockState();
        }
      }

      return accepted;
    }

    @Nullable
    @Override
    public FluidStack drain(FluidStack resource, boolean doDrain) {
      return null;
    }

    @Nullable
    @Override
    public FluidStack drain(int maxDrain, boolean doDrain) {
      return null;
    }
  }

  private class ItemHandler implements IItemHandlerModifiable {
    @Nonnull
    private ItemStack outputStack = ItemStack.EMPTY;

    @Override
    public int getSlots() {
      return 2;
    }

    @Nonnull
    @Override
    public ItemStack getStackInSlot(int slot) {
      return slot == 1 ? outputStack : ItemStack.EMPTY;
    }

    @Nonnull
    @Override
    public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
      if (slot != 0 || stack.isEmpty()) {
        return stack;
      }
      simulate |= world.isRemote;
      Tuple<DBEntry<IQuest>, IItemTask> questTask = getQuestTask(IItemTask.class);
      if (questTask == null) {
        return stack;
      }
      DBEntry<IQuest> quest = questTask.getFirst();
      IItemTask task = questTask.getSecond();
      stack = stack.copy();
      ItemStack remainder = task.submitItem(owner, quest, stack, !simulate);
      if (remainder.isEmpty() || ItemHandlerHelper.canItemStacksStack(remainder, stack)) {
        if (!simulate) {
          if (task.isComplete(owner)) {
            reset();
            syncBlockState();
          }
        }
        return remainder;
      } else {
        int maxCount = Math.min(remainder.getMaxStackSize(), 64);
        if (!outputStack.isEmpty() && !ItemHandlerHelper.canItemStacksStack(remainder, outputStack) ||
            remainder.getCount() + outputStack.getCount() > maxCount) {
          return stack;
        }
        if (!simulate) {
          int count = outputStack.getCount() + remainder.getCount();
          outputStack = remainder;
          remainder.setCount(count);
          if (task.isComplete(owner)) {
            reset();
            syncBlockState();
          }
          markDirty();
        }
        return ItemStack.EMPTY;
      }
    }

    @Nonnull
    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
      if (slot == 1) {
        ItemStack stack;
        if (simulate) {
          stack = outputStack.copy();
          stack.setCount(amount);
        } else {
          stack = outputStack.splitStack(amount);
          markDirty();
        }
        return stack;
      }
      return ItemStack.EMPTY;
    }

    @Override
    public int getSlotLimit(int slot) {
      return slot == 0 ? Integer.MAX_VALUE : 64;
    }

    @Override
    public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
      if (slot != 0) {
        return false;
      }
      Tuple<DBEntry<IQuest>, IItemTask> questTask = getQuestTask(IItemTask.class);
      return questTask != null && questTask.getSecond().canAcceptItem(owner, questTask.getFirst(), stack);
    }

    public void toTag(NBTTagCompound tag) {
      tag.setTag("OutputStack", outputStack.writeToNBT(new NBTTagCompound()));
    }

    public void fromTag(NBTTagCompound tag) {
      outputStack = new ItemStack(tag.getCompoundTag("OutputStack"));
    }

    @Override
    public void setStackInSlot(int slot, @Nonnull ItemStack stack) {
      if (slot == 0) {
        if (!stack.isEmpty()) {
          insertItem(0, stack, false);
        }
      } else {
        outputStack = stack.copy();
        markDirty();
      }
    }
  }

  private class EnergyHandler implements IEnergyStorage {
    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
      Tuple<DBEntry<IQuest>, IEnergyTask> questTask = getQuestTask(IEnergyTask.class);
      if (questTask == null) {
        return 0;
      }
      simulate |= world.isRemote;
      DBEntry<IQuest> quest = questTask.getFirst();
      IEnergyTask task = questTask.getSecond();
      int remainder = task.submitEnergy(owner, quest, maxReceive, !simulate);
      int accepted = maxReceive - remainder;
      if (!simulate) {
        if (task.isComplete(owner)) {
          reset();
          syncBlockState();
        }
      }
      return accepted;
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
      return 0;
    }

    @Override
    public int getEnergyStored() {
      return 0;
    }

    @Override
    public int getMaxEnergyStored() {
      return Integer.MAX_VALUE;
    }

    @Override
    public boolean canExtract() {
      return false;
    }

    @Override
    public boolean canReceive() {
      return true;
    }
  }
}
