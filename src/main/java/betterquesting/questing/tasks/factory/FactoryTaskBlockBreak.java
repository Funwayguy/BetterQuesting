package betterquesting.questing.tasks.factory;

import betterquesting.api.questing.tasks.ITask;
import betterquesting.api2.registry.IFactoryData;
import betterquesting.core.BetterQuesting;
import betterquesting.questing.tasks.TaskBlockBreak;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

public class FactoryTaskBlockBreak implements IFactoryData<ITask, NBTTagCompound>
{
	public static final FactoryTaskBlockBreak INSTANCE = new FactoryTaskBlockBreak();
	
	private final ResourceLocation REG_ID = new ResourceLocation(BetterQuesting.MODID_STD, "block_break");
	
	@Override
	public ResourceLocation getRegistryName()
	{
		return REG_ID;
	}

	@Override
	public TaskBlockBreak createNew()
	{
		return new TaskBlockBreak();
	}

	@Override
	public TaskBlockBreak loadFromData(NBTTagCompound json)
	{
		TaskBlockBreak task = new TaskBlockBreak();
		task.readFromNBT(json);
		return task;
	}
	
}
