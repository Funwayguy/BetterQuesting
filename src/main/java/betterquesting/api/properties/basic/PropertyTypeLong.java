package betterquesting.api.properties.basic;

import net.minecraft.nbt.INBT;
import net.minecraft.nbt.LongNBT;
import net.minecraft.nbt.NumberNBT;
import net.minecraft.util.ResourceLocation;

public class PropertyTypeLong extends PropertyTypeBase<Long>
{
	public PropertyTypeLong(ResourceLocation key, Long def)
	{
		super(key, def);
	}

	@Override
	public Long readValue(INBT nbt)
	{
		if(!(nbt instanceof NumberNBT))
		{
			return this.getDefault();
		}
		
		return ((NumberNBT)nbt).getLong();
	}

	@Override
	public INBT writeValue(Long value)
	{
		if(value == null)
		{
			return LongNBT.valueOf(this.getDefault());
		}
		
		return LongNBT.valueOf(value);
	}
}
