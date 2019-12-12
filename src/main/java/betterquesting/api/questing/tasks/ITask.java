package betterquesting.api.questing.tasks;

import betterquesting.api.questing.IQuest;
import betterquesting.api2.client.gui.misc.IGuiRect;
import betterquesting.api2.client.gui.panels.IGuiPanel;
import betterquesting.api2.storage.DBEntry;
import betterquesting.api2.storage.INBTProgress;
import betterquesting.api2.storage.INBTSaveLoad;
import betterquesting.api2.utils.ParticipantInfo;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.UUID;

public interface ITask extends INBTSaveLoad<CompoundNBT>, INBTProgress<CompoundNBT>
{
	String getUnlocalisedName();
	ResourceLocation getFactoryID();
	
	void detect(ParticipantInfo participant, DBEntry<IQuest> quest);
	
	boolean isComplete(UUID uuid);
	void setComplete(UUID uuid);
	
	void resetUser(@Nullable UUID uuid);
	
	@Nullable
	@OnlyIn(Dist.CLIENT)
    IGuiPanel getTaskGui(IGuiRect rect, DBEntry<IQuest> quest);
	
	@Nullable
	@OnlyIn(Dist.CLIENT)
	Screen getTaskEditor(Screen parent, DBEntry<IQuest> quest);
}
