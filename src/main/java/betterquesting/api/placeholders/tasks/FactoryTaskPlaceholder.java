package betterquesting.api.placeholders.tasks;

import net.minecraft.util.ResourceLocation;
import betterquesting.api.enums.EnumSaveType;
import betterquesting.api.misc.IFactory;
import com.google.gson.JsonObject;

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
	public TaskPlaceholder loadFromJson(JsonObject json)
	{
		TaskPlaceholder task = createNew();
		task.readFromJson(json, EnumSaveType.CONFIG);
		return task;
	}
}
