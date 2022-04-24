package betterquesting.api.placeholders.rewards;

import betterquesting.api.questing.IQuest;
import betterquesting.api.questing.rewards.IReward;
import betterquesting.api2.client.gui.misc.IGuiRect;
import betterquesting.api2.client.gui.panels.IGuiPanel;
import betterquesting.api2.storage.DBEntry;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

public class RewardPlaceholder implements IReward {
    private NBTTagCompound nbtSaved = new NBTTagCompound();

    public void setRewardConfigData(NBTTagCompound nbt) {
        nbtSaved = nbt;
    }

    public NBTTagCompound getRewardConfigData() {
        return nbtSaved;
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        nbt.setTag("orig_data", nbtSaved);

        return nbt;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        nbtSaved = nbt.getCompoundTag("orig_data");
    }

    @Override
    public String getUnlocalisedName() {
        return "betterquesting.placeholder";
    }

    @Override
    public ResourceLocation getFactoryID() {
        return FactoryRewardPlaceholder.INSTANCE.getRegistryName();
    }

    @Override
    public boolean canClaim(EntityPlayer player, DBEntry<IQuest> quest) {
        return false;
    }

    @Override
    public void claimReward(EntityPlayer player, DBEntry<IQuest> quest) {
    }

    @Override
    public IGuiPanel getRewardGui(IGuiRect rect, DBEntry<IQuest> quest) {
        return null;
    }

    @Override
    public GuiScreen getRewardEditor(GuiScreen parent, DBEntry<IQuest> quest) {
        return null;
    }
}
