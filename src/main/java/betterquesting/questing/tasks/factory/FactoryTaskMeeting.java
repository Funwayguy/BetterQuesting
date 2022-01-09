package betterquesting.questing.tasks.factory;

import betterquesting.api.questing.tasks.ITask;
import betterquesting.api2.registry.IFactoryData;
import betterquesting.core.BetterQuesting;
import betterquesting.questing.tasks.TaskMeeting;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

public class FactoryTaskMeeting implements IFactoryData<ITask, NBTTagCompound>
{
	public static final FactoryTaskMeeting INSTANCE = new FactoryTaskMeeting();
	
	@Override
	public ResourceLocation getRegistryName()
	{
		return new ResourceLocation(BetterQuesting.MODID_STD + ":meeting");
	}

	@Override
	public TaskMeeting createNew()
	{
		return new TaskMeeting();
	}

	@Override
	public TaskMeeting loadFromData(NBTTagCompound json)
	{
		TaskMeeting task = new TaskMeeting();
		task.readFromNBT(json);
		return task;
	}
	
}
