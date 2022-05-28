package betterquesting.blocks;

import betterquesting.api.api.ApiReference;
import betterquesting.api.api.QuestingAPI;
import betterquesting.api.properties.NativeProps;
import betterquesting.api.questing.IQuest;
import betterquesting.api.questing.IQuestDatabase;
import betterquesting.api.questing.tasks.IFluidTask;
import betterquesting.api.questing.tasks.IItemTask;
import betterquesting.api.questing.tasks.ITask;
import betterquesting.api2.storage.DBEntry;
import betterquesting.api2.utils.ParticipantInfo;
import betterquesting.storage.QuestSettings;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;
import net.minecraftforge.fluids.IFluidTank;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TileObservationStation extends TileEntity {
    public UUID owner = null;
    private static final String NBT_KEY_OWNER = "owner";

    @Override
    public void updateEntity() {
        if (this.owner == null
            || QuestSettings.INSTANCE.getProperty(NativeProps.EDIT_MODE)
            || this.worldObj == null
            || this.worldObj.isRemote
            || this.worldObj.getTotalWorldTime() % 20 != 0
        )
            return;

        final EntityPlayerMP player = getPlayerByUUID(this.owner);
        if (player == null) return;

        ParticipantInfo pInfo = new ParticipantInfo(player);

        List<ItemStack> items = new ArrayList<>();
        List<FluidStack> fluids = new ArrayList<>();
        for (ForgeDirection side : ForgeDirection.VALID_DIRECTIONS) {
            TileEntity tile = this.worldObj.getTileEntity(
                this.xCoord + side.offsetX,
                this.yCoord + side.offsetY,
                this.zCoord + side.offsetZ
            );
            if (tile instanceof IInventory) {
                IInventory inventory = (IInventory) tile;
                for (int i = 0; i < inventory.getSizeInventory(); i++) {
                    ItemStack stack = inventory.getStackInSlot(i);
                    if (stack != null && 0 < stack.stackSize) {
                        items.add(stack);
                    }
                }
            }
            if (tile instanceof IFluidTank) {
                IFluidTank tank = (IFluidTank) tile;
                FluidStack fluid = tank.getFluid();
                if (fluid != null && 0 < fluid.amount) {
                    fluids.add(fluid);
                }
            }
            if (tile instanceof IFluidHandler) {
                IFluidHandler handler = (IFluidHandler) tile;
                for (FluidTankInfo tank : handler.getTankInfo(ForgeDirection.UNKNOWN)) {
                    FluidStack fluid = tank.fluid;
                    if (fluid != null && 0 < fluid.amount) {
                        fluids.add(fluid);
                    }
                }
            }
        }

        if (items.isEmpty() && fluids.isEmpty()) {
            return;
        }

        final IQuestDatabase questDB = QuestingAPI.getAPI(ApiReference.QUEST_DB);
        if (questDB == null) return;

        for (DBEntry<IQuest> questEntry : questDB.bulkLookup(pInfo.getSharedQuests())) {
            for (DBEntry<ITask> taskEntry : questEntry.getValue().getTasks().getEntries()) {
                final ITask task = taskEntry.getValue();
                if (task instanceof IItemTask && !items.isEmpty()) {
                    ((IItemTask) task).retrieveItems(pInfo, questEntry, items.toArray(new ItemStack[0]));
                }
                if (task instanceof IFluidTask && !fluids.isEmpty()) {
                    ((IFluidTask) task).retrieveFluids(pInfo, questEntry, fluids.toArray(new FluidStack[0]));
                }
            }
        }
    }

    @SuppressWarnings({"unchecked", "DuplicatedCode"})
    @Nullable
    private EntityPlayerMP getPlayerByUUID(UUID uuid) {
        MinecraftServer server = MinecraftServer.getServer();
        if (server == null) return null;

        for (EntityPlayerMP player : (List<EntityPlayerMP>) server.getConfigurationManager().playerEntityList) {
            if (player.getGameProfile().getId().equals(uuid)) return player;
        }

        return null;
    }

    @Override
    public void readFromNBT(@Nonnull NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        if (!nbt.hasKey(NBT_KEY_OWNER, Constants.NBT.TAG_STRING)) return;
        try {
            this.owner = UUID.fromString(nbt.getString(NBT_KEY_OWNER));
        } catch (Exception ignored) {
            this.owner = null;
        }
    }

    @Override
    public void writeToNBT(@Nonnull NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        if (this.owner == null) return;
        nbt.setString(NBT_KEY_OWNER, this.owner.toString());
    }
}
