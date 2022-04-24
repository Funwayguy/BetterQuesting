package betterquesting.api.placeholders.tasks;

import betterquesting.api.questing.IQuest;
import betterquesting.api.questing.tasks.ITask;
import betterquesting.api2.client.gui.misc.IGuiRect;
import betterquesting.api2.client.gui.panels.IGuiPanel;
import betterquesting.api2.storage.DBEntry;
import betterquesting.api2.utils.ParticipantInfo;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

public class TaskPlaceholder implements ITask {
    private NBTTagCompound nbtData = new NBTTagCompound();

    public void setTaskConfigData(NBTTagCompound nbt) {
        nbtData.setTag("orig_data", nbt);
    }

    public void setTaskProgressData(NBTTagCompound nbt) {
        nbtData.setTag("orig_prog", nbt);
    }

    public NBTTagCompound getTaskConfigData() {
        return nbtData.getCompoundTag("orig_data");
    }

    public NBTTagCompound getTaskProgressData() {
        return nbtData.getCompoundTag("orig_prog");
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        nbt.setTag("orig_data", nbtData.getCompoundTag("orig_data"));
        return nbt;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        nbtData.setTag("orig_data", nbt.getCompoundTag("orig_data"));
    }

    @Override
    public NBTTagCompound writeProgressToNBT(NBTTagCompound nbt, @Nullable List<UUID> users) {
        nbt.setTag("orig_prog", nbtData.getCompoundTag("orig_prog"));
        return nbt;
    }

    @Override
    public void readProgressFromNBT(NBTTagCompound nbt, boolean merge) {
        nbtData.setTag("orig_prog", nbt.getCompoundTag("orig_prog"));
    }

    @Override
    public String getUnlocalisedName() {
        return "betterquesting.placeholder";
    }

    @Override
    public ResourceLocation getFactoryID() {
        return FactoryTaskPlaceholder.INSTANCE.getRegistryName();
    }

    @Override
    public void detect(ParticipantInfo participant, DBEntry<IQuest> quest) {
    }

    @Override
    public boolean isComplete(UUID uuid) {
        return false;
    }

    @Override
    public void setComplete(UUID uuid) {
    }

    @Override
    public void resetUser(UUID uuid) {
    }

    @Override
    public IGuiPanel getTaskGui(IGuiRect rect, DBEntry<IQuest> quest) {
        return null;
    }

    @Override
    public GuiScreen getTaskEditor(GuiScreen parent, DBEntry<IQuest> quest) {
        return null;
    }
}
