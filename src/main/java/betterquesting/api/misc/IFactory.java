package betterquesting.api.misc;

import net.minecraft.util.ResourceLocation;
import com.google.gson.JsonObject;

public interface IFactory<T>
{
	public ResourceLocation getRegistryName();
	
	public T createNew();
	public T loadFromJson(JsonObject json);
}
