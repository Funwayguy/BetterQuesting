package betterquesting.api.properties;

import net.minecraft.util.ResourceLocation;
import com.google.gson.JsonElement;

public interface IPropertyType<T>
{
	public ResourceLocation getKey();
	public T getDefault();
	
	public T readValue(JsonElement json);
	public JsonElement writeValue(T value);
}
