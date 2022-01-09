package betterquesting.questing.tasks.factory;

import betterquesting.api.questing.tasks.ITask;
import betterquesting.api2.registry.IFactoryData;
import betterquesting.core.BetterQuesting;
import betterquesting.questing.tasks.TaskInteractEntity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

public class FactoryTaskInteractEntity implements IFactoryData<ITask, NBTTagCompound>
{
    public static final FactoryTaskInteractEntity INSTANCE = new FactoryTaskInteractEntity();
    
	private final ResourceLocation REG_ID = new ResourceLocation(BetterQuesting.MODID_STD, "interact_entity");
	
    @Override
    public ResourceLocation getRegistryName()
    {
        return REG_ID;
    }
    
    @Override
    public TaskInteractEntity createNew()
    {
        return new TaskInteractEntity();
    }
    
    @Override
    public TaskInteractEntity loadFromData(NBTTagCompound nbt)
    {
        TaskInteractEntity task = new TaskInteractEntity();
        task.readFromNBT(nbt);
        return task;
    }
}
