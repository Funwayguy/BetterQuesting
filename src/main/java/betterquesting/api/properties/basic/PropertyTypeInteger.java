package betterquesting.api.properties.basic;

import net.minecraft.nbt.INBT;
import net.minecraft.nbt.IntNBT;
import net.minecraft.nbt.NumberNBT;
import net.minecraft.util.ResourceLocation;

public class PropertyTypeInteger extends PropertyTypeBase<Integer>
{
	public PropertyTypeInteger(ResourceLocation key, Integer def)
	{
		super(key, def);
	}

	@Override
	public Integer readValue(INBT nbt)
	{
		if(!(nbt instanceof NumberNBT))
		{
			return this.getDefault();
		}
		
		return ((NumberNBT)nbt).getInt();
	}

	@Override
	public IntNBT writeValue(Integer value)
	{
		if(value == null)
		{
			return IntNBT.valueOf(this.getDefault());
		}
		
		return IntNBT.valueOf(value);
	}
}
