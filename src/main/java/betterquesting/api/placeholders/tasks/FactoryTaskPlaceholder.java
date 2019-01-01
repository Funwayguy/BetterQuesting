package betterquesting.api.placeholders.tasks;

import betterquesting.api.misc.IFactory;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

public class FactoryTaskPlaceholder implements IFactory<TaskPlaceholder>
{
	public static final FactoryTaskPlaceholder INSTANCE = new FactoryTaskPlaceholder();
	
	private final ResourceLocation ID = new ResourceLocation("betterquesting:placeholder");
	
	private FactoryTaskPlaceholder()
	{
	}
	
	@Override
	public ResourceLocation getRegistryName()
	{
		return ID;
	}
	
	@Override
	public TaskPlaceholder createNew()
	{
		return new TaskPlaceholder();
	}
	
	@Override
	public TaskPlaceholder loadFromNBT(NBTTagCompound nbt)
	{
		TaskPlaceholder task = createNew();
		task.readFromNBT(nbt);
		return task;
	}
}
