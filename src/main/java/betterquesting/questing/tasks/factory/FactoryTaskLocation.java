package betterquesting.questing.tasks.factory;

import betterquesting.api.questing.tasks.ITask;
import betterquesting.api2.registry.IFactoryData;
import betterquesting.core.BetterQuesting;
import betterquesting.questing.tasks.TaskLocation;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

public class FactoryTaskLocation implements IFactoryData<ITask, NBTTagCompound>
{
	public static final FactoryTaskLocation INSTANCE = new FactoryTaskLocation();
	
	@Override
	public ResourceLocation getRegistryName()
	{
		return new ResourceLocation(BetterQuesting.MODID_STD + ":location");
	}

	@Override
	public TaskLocation createNew()
	{
		return new TaskLocation();
	}

	@Override
	public TaskLocation loadFromData(NBTTagCompound json)
	{
		TaskLocation task = new TaskLocation();
		task.readFromNBT(json);
		return task;
	}
	
}
