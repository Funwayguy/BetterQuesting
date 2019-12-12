package betterquesting.api.properties.basic;

import net.minecraft.nbt.ByteNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.NumberNBT;
import net.minecraft.util.ResourceLocation;

public class PropertyTypeBoolean extends PropertyTypeBase<Boolean>
{
	public PropertyTypeBoolean(ResourceLocation key, Boolean def)
	{
		super(key, def);
	}
	
	@Override
	public Boolean readValue(INBT nbt)
	{
		if(nbt == null || nbt.getId() < 1 || nbt.getId() > 6)
		{
			return this.getDefault();
		}
		
		try
		{
			return ((NumberNBT)nbt).getByte() > 0;
		} catch(Exception e)
		{
			return this.getDefault();
		}
	}
	
	@Override
	public ByteNBT writeValue(Boolean value)
	{
		if(value == null)
		{
			return new ByteNBT(this.getDefault() ? (byte)1 : (byte)0);
		}
		
		return new ByteNBT(value ? (byte)1 : (byte)0);
	}
}