package betterquesting.api.quests.properties;

import net.minecraft.util.ResourceLocation;
import com.google.gson.JsonElement;

public interface IQuestProperty<T>
{
	public ResourceLocation getKey();
	public T getDefault();
	
	public T readValue(JsonElement json);
	public JsonElement writeValue(T value);
}
