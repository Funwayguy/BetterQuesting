package betterquesting.api.properties.basic;

import net.minecraft.nbt.DoubleNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.NumberNBT;
import net.minecraft.util.ResourceLocation;

public class PropertyTypeDouble extends PropertyTypeBase<Double>
{
	public PropertyTypeDouble(ResourceLocation key, Double def)
	{
		super(key, def);
	}

	@Override
	public Double readValue(INBT nbt)
	{
		if(!(nbt instanceof NumberNBT))
		{
			return this.getDefault();
		}
		
		return ((NumberNBT)nbt).getDouble();
	}

	@Override
	public DoubleNBT writeValue(Double value)
	{
		if(value == null)
		{
			return new DoubleNBT(this.getDefault());
		}
		
		return new DoubleNBT(value);
	}
}
