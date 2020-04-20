package betterquesting.api.properties.basic;

import net.minecraft.nbt.INBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.util.ResourceLocation;

public class PropertyTypeEnum<E extends Enum<E>> extends PropertyTypeBase<E>
{
	private final Class<E> eClazz;
	
	public PropertyTypeEnum(ResourceLocation key, E def)
	{
		super(key, def);
		
		eClazz = def.getDeclaringClass();
	}
	
	@Override
	public E readValue(INBT nbt)
	{
		if(nbt == null || nbt.getId() != 8)
		{
			return this.getDefault();
		}
		
		try
		{
			return Enum.valueOf(eClazz, nbt.getString());
		} catch(Exception e)
		{
			return this.getDefault();
		}
	}
	
	@Override
	public INBT writeValue(E value)
	{
		if(value == null)
		{
			return StringNBT.valueOf(this.getDefault().toString());
		}
		
		return StringNBT.valueOf(value.toString());
	}
}
