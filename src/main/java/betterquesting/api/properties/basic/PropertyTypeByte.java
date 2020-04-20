package betterquesting.api.properties.basic;

import net.minecraft.nbt.ByteNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.NumberNBT;
import net.minecraft.util.ResourceLocation;

public class PropertyTypeByte extends PropertyTypeBase<Byte>
{
	public PropertyTypeByte(ResourceLocation key, Byte def)
	{
		super(key, def);
	}

	@Override
	public Byte readValue(INBT nbt)
	{
		if(!(nbt instanceof NumberNBT))
		{
			return this.getDefault();
		}
		
		return ((NumberNBT)nbt).getByte();
	}

	@Override
	public ByteNBT writeValue(Byte value)
	{
		if(value == null)
		{
			return ByteNBT.valueOf(this.getDefault());
		}
		
		return ByteNBT.valueOf(value);
	}
}
