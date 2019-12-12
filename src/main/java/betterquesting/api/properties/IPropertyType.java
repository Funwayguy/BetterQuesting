package betterquesting.api.properties;

import net.minecraft.nbt.INBT;
import net.minecraft.util.ResourceLocation;

public interface IPropertyType<T>
{
	ResourceLocation getKey();
	T getDefault();
	
	T readValue(INBT nbt);
	INBT writeValue(T value);
}
