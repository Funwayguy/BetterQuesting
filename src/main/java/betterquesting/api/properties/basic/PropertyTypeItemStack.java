package betterquesting.api.properties.basic;

import betterquesting.api.utils.BigItemStack;
import betterquesting.api.utils.JsonHelper;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.ResourceLocation;

public class PropertyTypeItemStack extends PropertyTypeBase<BigItemStack>
{
	public PropertyTypeItemStack(ResourceLocation key, BigItemStack def)
	{
		super(key, def);
	}
	
	@Override
	public BigItemStack readValue(INBT nbt)
	{
		if(nbt == null || nbt.getId() != 10)
		{
			return this.getDefault();
		}
		
		return JsonHelper.JsonToItemStack((CompoundNBT)nbt);
	}
	
	@Override
	public CompoundNBT writeValue(BigItemStack value)
	{
		CompoundNBT nbt = new CompoundNBT();
		
		if(value == null || value.getBaseStack() == null)
		{
			getDefault().writeToNBT(nbt);
		} else
		{
			value.writeToNBT(nbt);
		}
		
		return nbt;
	}
}
