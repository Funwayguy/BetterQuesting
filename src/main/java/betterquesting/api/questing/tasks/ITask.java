package betterquesting.api.questing.tasks;

import betterquesting.api.jdoc.IJsonDoc;
import betterquesting.api.misc.INBTSaveLoad;
import betterquesting.api.questing.IQuest;
import betterquesting.api2.client.gui.misc.IGuiRect;
import betterquesting.api2.client.gui.panels.IGuiPanel;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.UUID;
import java.util.List;

public interface ITask extends INBTSaveLoad<NBTTagCompound>
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

	/**
	 * Writes users named in userFilter progress into the nbt tag.
	 * This is used only to notify related users of their progress change and not
	 * for saving. Server side save uses writeToNBT.
	 */
	NBTTagCompound writeProgressToJson(NBTTagCompound nbt, List<UUID> userFilter);
	
	@SideOnly(Side.CLIENT)
    IGuiPanel getTaskGui(IGuiRect rect, IQuest quest);
	
	@Nullable
	@SideOnly(Side.CLIENT)
	GuiScreen getTaskEditor(GuiScreen parent, IQuest quest);
}
