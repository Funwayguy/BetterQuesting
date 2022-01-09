package betterquesting.questing.tasks.factory;

import betterquesting.api.questing.tasks.ITask;
import betterquesting.api2.registry.IFactoryData;
import betterquesting.core.BetterQuesting;
import betterquesting.questing.tasks.TaskCrafting;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

public class FactoryTaskCrafting implements IFactoryData<ITask, NBTTagCompound>
{
	public static final FactoryTaskCrafting INSTANCE = new FactoryTaskCrafting();
	
	@Override
	public ResourceLocation getRegistryName()
	{
		return new ResourceLocation(BetterQuesting.MODID_STD + ":crafting");
	}

	@Override
	public TaskCrafting createNew()
	{
		return new TaskCrafting();
	}

	@Override
	public TaskCrafting loadFromData(NBTTagCompound json)
	{
		TaskCrafting task = new TaskCrafting();
		task.readFromNBT(json);
		return task;
	}
	
}
