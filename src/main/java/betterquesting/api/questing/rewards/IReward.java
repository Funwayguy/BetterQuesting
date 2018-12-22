package betterquesting.api.questing.rewards;

import betterquesting.api.jdoc.IJsonDoc;
import betterquesting.api.misc.INBTSaveLoad;
import betterquesting.api.questing.IQuest;
import betterquesting.api2.client.gui.panels.IGuiPanel;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;

public interface IReward extends INBTSaveLoad<NBTTagCompound>
{
	String getUnlocalisedName();
	ResourceLocation getFactoryID();
	
	boolean canClaim(EntityPlayer player, IQuest quest);
	void claimReward(EntityPlayer player, IQuest quest);
	
	@Deprecated
	IJsonDoc getDocumentation();
	
	@SideOnly(Side.CLIENT)
    IGuiPanel getRewardGui(int x, int y, int w, int h, IQuest quest);
	
	@Nullable
	@SideOnly(Side.CLIENT)
	GuiScreen getRewardEditor(GuiScreen parent, IQuest quest);
}
