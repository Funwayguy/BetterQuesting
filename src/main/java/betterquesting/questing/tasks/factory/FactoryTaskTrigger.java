package betterquesting.questing.tasks.factory;

import betterquesting.api.questing.tasks.ITask;
import betterquesting.api2.registry.IFactoryData;
import betterquesting.core.BetterQuesting;
import betterquesting.questing.tasks.TaskTrigger;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

public class FactoryTaskTrigger implements IFactoryData<ITask, NBTTagCompound>
{
    public static final FactoryTaskTrigger INSTANCE = new FactoryTaskTrigger();
    
	private final ResourceLocation REG_ID = new ResourceLocation(BetterQuesting.MODID_STD, "trigger");
	
    @Override
    public ResourceLocation getRegistryName()
    {
        return REG_ID;
    }
    
    @Override
    public TaskTrigger createNew()
    {
        return new TaskTrigger();
    }
    
    @Override
    public TaskTrigger loadFromData(NBTTagCompound nbt)
    {
        TaskTrigger task = new TaskTrigger();
        task.readFromNBT(nbt);
        return task;
    }
}
