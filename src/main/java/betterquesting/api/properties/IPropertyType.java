package betterquesting.api.properties;

import net.minecraft.nbt.NBTBase;
import net.minecraft.util.ResourceLocation;

public interface IPropertyType<T>
{
	public ResourceLocation getKey();
	public T getDefault();
	
	public T readValue(NBTBase nbt);
	public NBTBase writeValue(T value);
}
