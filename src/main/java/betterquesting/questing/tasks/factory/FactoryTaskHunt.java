package betterquesting.questing.tasks.factory;

import betterquesting.api.questing.tasks.ITask;
import betterquesting.api2.registry.IFactoryData;
import betterquesting.core.BetterQuesting;
import betterquesting.questing.tasks.TaskHunt;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

public class FactoryTaskHunt implements IFactoryData<ITask, NBTTagCompound>
{
	public static final FactoryTaskHunt INSTANCE = new FactoryTaskHunt();
	
	@Override
	public ResourceLocation getRegistryName()
	{
		return new ResourceLocation(BetterQuesting.MODID_STD + ":hunt");
	}

	@Override
	public TaskHunt createNew()
	{
		return new TaskHunt();
	}

	@Override
	public TaskHunt loadFromData(NBTTagCompound json)
	{
		TaskHunt task = new TaskHunt();
		task.readFromNBT(json);
		return task;
	}
	
}
