package betterquesting.api.properties.basic;

import net.minecraft.nbt.FloatNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.NumberNBT;
import net.minecraft.util.ResourceLocation;

public class PropertyTypeFloat extends PropertyTypeBase<Float>
{
	public PropertyTypeFloat(ResourceLocation key, Float def)
	{
		super(key, def);
	}

	@Override
	public Float readValue(INBT nbt)
	{
		if(!(nbt instanceof NumberNBT))
		{
			return this.getDefault();
		}
		
		return ((NumberNBT)nbt).getFloat();
	}

	@Override
	public FloatNBT writeValue(Float value)
	{
		if(value == null)
		{
			return new FloatNBT(this.getDefault());
		}
		
		return new FloatNBT(value);
	}
}
