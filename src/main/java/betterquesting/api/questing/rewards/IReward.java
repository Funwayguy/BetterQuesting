package betterquesting.api.questing.rewards;

import betterquesting.api.questing.IQuest;
import betterquesting.api2.client.gui.misc.IGuiRect;
import betterquesting.api2.client.gui.panels.IGuiPanel;
import betterquesting.api2.storage.DBEntry;
import betterquesting.api2.storage.INBTSaveLoad;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;

public interface IReward extends INBTSaveLoad<NBTTagCompound> {
    String getUnlocalisedName();

    ResourceLocation getFactoryID();

    boolean canClaim(EntityPlayer player, DBEntry<IQuest> quest);

    void claimReward(EntityPlayer player, DBEntry<IQuest> quest);

    @SideOnly(Side.CLIENT)
    IGuiPanel getRewardGui(IGuiRect rect, DBEntry<IQuest> quest);

    @Nullable
    @SideOnly(Side.CLIENT)
    GuiScreen getRewardEditor(GuiScreen parent, DBEntry<IQuest> quest);
}
