package betterquesting.api.questing.tasks;

import betterquesting.api.questing.IQuest;
import betterquesting.api2.client.gui.misc.IGuiRect;
import betterquesting.api2.client.gui.panels.IGuiPanel;
import betterquesting.api2.storage.DBEntry;
import betterquesting.api2.storage.INBTProgress;
import betterquesting.api2.storage.INBTSaveLoad;
import betterquesting.api2.utils.ParticipantInfo;
import net.minecraft.client.gui.GuiScreen;
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
	
	void detect(ParticipantInfo participant, DBEntry<IQuest> quest);
	
	boolean isComplete(UUID uuid);
	void setComplete(UUID uuid);
	
	void resetUser(@Nullable UUID uuid);
	
	@Nullable
	@SideOnly(Side.CLIENT)
    IGuiPanel getTaskGui(IGuiRect rect, DBEntry<IQuest> quest);
	
	@Nullable
	@SideOnly(Side.CLIENT)
	GuiScreen getTaskEditor(GuiScreen parent, DBEntry<IQuest> quest);

	default boolean displaysCenteredAlone() {
		return false;
	}
}
