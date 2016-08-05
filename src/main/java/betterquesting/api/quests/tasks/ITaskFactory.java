package betterquesting.api.quests.tasks;

import com.google.gson.JsonObject;
import net.minecraft.util.ResourceLocation;

public interface ITaskFactory<T extends ITaskBase>
{
	public ResourceLocation getRegistryName();
	public T createTask();
	public T LoadTask(JsonObject json);
}
