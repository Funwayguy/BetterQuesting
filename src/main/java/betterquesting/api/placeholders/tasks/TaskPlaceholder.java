package betterquesting.api.placeholders.tasks;

import betterquesting.api.questing.IQuest;
import betterquesting.api.questing.tasks.ITask;
import betterquesting.api2.client.gui.misc.IGuiRect;
import betterquesting.api2.client.gui.panels.IGuiPanel;
import betterquesting.api2.storage.DBEntry;
import betterquesting.api2.utils.ParticipantInfo;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

public class TaskPlaceholder implements ITask
{
	private CompoundNBT nbtData = new CompoundNBT();
	
	public void setTaskConfigData(CompoundNBT nbt)
	{
        nbtData.put("orig_data", nbt);
	}
	
	public void setTaskProgressData(CompoundNBT nbt)
    {
        nbtData.put("orig_prog", nbt);
    }
	
	public CompoundNBT getTaskConfigData()
	{
        return nbtData.getCompound("orig_data");
	}
	
	public CompoundNBT getTaskProgressData()
    {
        return nbtData.getCompound("orig_prog");
    }
	
	@Override
	public CompoundNBT writeToNBT(CompoundNBT nbt)
	{
        nbt.put("orig_data", nbtData.getCompound("orig_data"));
		return nbt;
	}
	
	@Override
	public void readFromNBT(CompoundNBT nbt)
	{
        nbtData.put("orig_data", nbt.getCompound("orig_data"));
	}
	
	@Override
    public CompoundNBT writeProgressToNBT(CompoundNBT nbt, @Nullable List<UUID> users)
    {
        nbt.put("orig_prog", nbtData.getCompound("orig_prog"));
        return nbt;
    }
    
    @Override
    public void readProgressFromNBT(CompoundNBT nbt, boolean merge)
    {
        nbtData.put("orig_prog", nbt.getCompound("orig_prog"));
    }
	
	@Override
	public String getUnlocalisedName()
	{
		return "betterquesting.placeholder";
	}
	
	@Override
	public ResourceLocation getFactoryID()
	{
		return FactoryTaskPlaceholder.INSTANCE.getRegistryName();
	}
	
	@Override
	public void detect(ParticipantInfo participant, DBEntry<IQuest> quest)
	{
	}
	
	@Override
	public boolean isComplete(UUID uuid)
	{
		return false;
	}
	
	@Override
	public void setComplete(UUID uuid)
	{
	}
	
	@Override
	public void resetUser(UUID uuid)
	{
	}
	
	@Override
	public IGuiPanel getTaskGui(IGuiRect rect, DBEntry<IQuest> quest)
	{
		return null;
	}
	
	@Override
	public Screen getTaskEditor(Screen parent, DBEntry<IQuest> quest)
	{
		return null;
	}
}
