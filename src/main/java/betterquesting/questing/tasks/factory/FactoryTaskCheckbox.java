package betterquesting.questing.tasks.factory;

import betterquesting.api.questing.tasks.ITask;
import betterquesting.api2.registry.IFactoryData;
import betterquesting.core.BetterQuesting;
import betterquesting.questing.tasks.TaskCheckbox;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

public class FactoryTaskCheckbox implements IFactoryData<ITask, NBTTagCompound>
{
	public static final FactoryTaskCheckbox INSTANCE = new FactoryTaskCheckbox();
	
	@Override
	public ResourceLocation getRegistryName()
	{
		return new ResourceLocation(BetterQuesting.MODID_STD + ":checkbox");
	}

	@Override
	public TaskCheckbox createNew()
	{
		return new TaskCheckbox();
	}

	@Override
	public TaskCheckbox loadFromData(NBTTagCompound json)
	{
		TaskCheckbox task = new TaskCheckbox();
		task.readFromNBT(json);
		return task;
	}
	
}
