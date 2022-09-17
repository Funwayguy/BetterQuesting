package betterquesting.api.properties.basic;

import net.minecraft.util.ResourceLocation;
import betterquesting.api.properties.IPropertyType;

public abstract class PropertyTypeBase<T> implements IPropertyType<T>
{
	private final ResourceLocation key;
	private final T def;
	
	public PropertyTypeBase(ResourceLocation key, T def)
	{
		this.key = key;
		this.def = def;
	}
	
	@Override
	public ResourceLocation getKey()
	{
		return key;
	}
	
	@Override
	public T getDefault()
	{
		return def;
	}
}
