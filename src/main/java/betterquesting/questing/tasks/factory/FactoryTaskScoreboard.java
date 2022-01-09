package betterquesting.questing.tasks.factory;

import betterquesting.api.questing.tasks.ITask;
import betterquesting.api2.registry.IFactoryData;
import betterquesting.core.BetterQuesting;
import betterquesting.questing.tasks.TaskScoreboard;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

public class FactoryTaskScoreboard implements IFactoryData<ITask, NBTTagCompound>
{
	public static final FactoryTaskScoreboard INSTANCE = new FactoryTaskScoreboard();
	
	@Override
	public ResourceLocation getRegistryName()
	{
		return new ResourceLocation(BetterQuesting.MODID_STD + ":scoreboard");
	}

	@Override
	public TaskScoreboard createNew()
	{
		return new TaskScoreboard();
	}

	@Override
	public TaskScoreboard loadFromData(NBTTagCompound json)
	{
		TaskScoreboard task = new TaskScoreboard();
		task.readFromNBT(json);
		return task;
	}
	
}
