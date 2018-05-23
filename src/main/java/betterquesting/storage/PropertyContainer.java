package betterquesting.storage;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
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
		
		NBTTagCompound jProp = getDomain(prop.getKey());
		
		if(!jProp.hasKey(prop.getKey().getResourcePath()))
		{
			return def;
		}
		
		return prop.readValue(jProp.getTag(prop.getKey().getResourcePath()));
	}
	
	@Override
	public boolean hasProperty(IPropertyType<?> prop)
	{
		if(prop == null)
		{
			return false;
		}
		
		return getDomain(prop.getKey()).hasKey(prop.getKey().getResourcePath());
	}
	
	@Override
	public <T> void setProperty(IPropertyType<T> prop, T value)
	{
		if(prop == null || value == null)
		{
			return;
		}
		
		NBTTagCompound dom = getDomain(prop.getKey());
		dom.setTag(prop.getKey().getResourcePath(), prop.writeValue(value));
		nbtInfo.setTag(prop.getKey().getResourceDomain(), dom);
	}
	
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt)
	{
		nbt.merge(nbtInfo);
		return nbt;
	}
	
	@Override
	public void readFromNBT(NBTTagCompound nbt)
	{
		nbtInfo = new NBTTagCompound();
		nbtInfo.merge(nbt);
	}
	
	private NBTTagCompound getDomain(ResourceLocation res)
	{
		return nbtInfo.getCompoundTag(res.getResourceDomain());
	}
}
