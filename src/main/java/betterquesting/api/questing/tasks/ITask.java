package betterquesting.api.questing.tasks;

import java.util.List;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import betterquesting.api.client.gui.misc.IGuiEmbedded;
import betterquesting.api.enums.EnumSaveType;
import betterquesting.api.jdoc.IJsonDoc;
import betterquesting.api.misc.INBTSaveLoad;
import betterquesting.api.questing.IQuest;

public interface ITask extends INBTSaveLoad<NBTTagCompound>
{
	public String getUnlocalisedName();
	public ResourceLocation getFactoryID();
	
	public void detect(EntityPlayer player, IQuest quest);
	
	public boolean isComplete(UUID uuid);
	public void setComplete(UUID uuid);
	
	public void resetUser(UUID uuid);
	public void resetAll();
	
	public IJsonDoc getDocumentation();

	/**
	 * Writes users named in userFilter progress into the nbt tag.
	 * This is used only to notify related users of their progress change and not
	 * for saving. Server side save uses writeToNBT.
	 */
	NBTTagCompound writeProgressToJson(NBTTagCompound nbt, List<UUID> userFilter);
	
	@SideOnly(Side.CLIENT)
	public IGuiEmbedded getTaskGui(int x, int y, int w, int h, IQuest quest);
	
	@Nullable
	@SideOnly(Side.CLIENT)
	public GuiScreen getTaskEditor(GuiScreen parent, IQuest quest);
}
