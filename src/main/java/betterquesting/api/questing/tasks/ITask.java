package betterquesting.api.questing.tasks;

import betterquesting.api.jdoc.IJsonDoc;
import betterquesting.api.questing.IQuest;
import betterquesting.api2.client.gui.misc.IGuiRect;
import betterquesting.api2.client.gui.panels.IGuiPanel;
import betterquesting.api2.storage.INBTProgress;
import betterquesting.api2.storage.INBTSaveLoad;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.UUID;

public interface ITask extends INBTSaveLoad<NBTTagCompound>, INBTProgress<NBTTagCompound>
{
	String getUnlocalisedName();
	ResourceLocation getFactoryID();
	
	void detect(EntityPlayer player, IQuest quest);
	
	boolean isComplete(UUID uuid);
	void setComplete(UUID uuid);
	
	void resetUser(UUID uuid);
	void resetAll();
	
	@Deprecated
	IJsonDoc getDocumentation();
	
	@SideOnly(Side.CLIENT)
    IGuiPanel getTaskGui(IGuiRect rect, IQuest quest);
	
	@Nullable
	@SideOnly(Side.CLIENT)
	GuiScreen getTaskEditor(GuiScreen parent, IQuest quest);
}
