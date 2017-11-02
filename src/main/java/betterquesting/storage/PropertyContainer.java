package betterquesting.storage;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import betterquesting.api.enums.EnumSaveType;
import betterquesting.api.properties.IPropertyContainer;
import betterquesting.api.properties.IPropertyType;

public class PropertyContainer implements IPropertyContainer
{
	private NBTTagCompound nbtInfo = new NBTTagCompound();
	
	@Override
	public <T> T getProperty(IPropertyType<T> prop)
	{
		if(prop == null)
		{
			return null;
		}
		
		return getProperty(prop, prop.getDefault());
	}
	
	@Override
	public <T> T getProperty(IPropertyType<T> prop, T def)
	{
		if(prop == null)
		{
			return null;
		}
		
		NBTBase jProp = getJsonDomain(prop.getKey()).getTag(prop.getKey().getResourcePath());
		
		if(jProp == null)
		{
			return def;
		}
		
		return prop.readValue(jProp);
	}
	
	@Override
	public boolean hasProperty(IPropertyType<?> prop)
	{
		if(prop == null)
		{
			return false;
		}
		
		return getJsonDomain(prop.getKey()).hasKey(prop.getKey().getResourcePath());
	}
	
	@Override
	public <T> void setProperty(IPropertyType<T> prop, T value)
	{
		if(prop == null || value == null)
		{
			return;
		}
		
		NBTTagCompound dom = getJsonDomain(prop.getKey());
		dom.setTag(prop.getKey().getResourcePath(), prop.writeValue(value));
		nbtInfo.setTag(prop.getKey().getResourceDomain(), dom);
	}
	
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt, EnumSaveType saveType)
	{
		nbt.merge(nbtInfo);
		return nbt;
	}
	
	@Override
	public void readFromNBT(NBTTagCompound nbt, EnumSaveType saveType)
	{
		nbtInfo = new NBTTagCompound();
		nbtInfo.merge(nbt);
	}
	
	private NBTTagCompound getJsonDomain(ResourceLocation res)
	{
		return nbtInfo.getCompoundTag(res.getResourceDomain());
	}
}
