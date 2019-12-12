package betterquesting.api.properties.basic;

import net.minecraft.nbt.INBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.util.ResourceLocation;

public class PropertyTypeString extends PropertyTypeBase<String>
{
	public PropertyTypeString(ResourceLocation key, String def)
	{
		super(key, def);
	}
	
	@Override
	public String readValue(INBT nbt)
	{
		if(nbt == null || nbt.getId() != 8)
		{
			return this.getDefault();
		}
		
		return nbt.getString();
	}
	
	@Override
	public StringNBT writeValue(String value)
	{
		if(value == null)
		{
			return new StringNBT(this.getDefault());
		}
		
		return new StringNBT(value);
	}
}
