package betterquesting.api.properties;

import net.minecraft.nbt.NBTTagCompound;
import betterquesting.api.misc.INBTSaveLoad;

public interface IPropertyContainer extends INBTSaveLoad<NBTTagCompound>
{
	public <T> T getProperty(IPropertyType<T> prop);
	public <T> T getProperty(IPropertyType<T> prop, T def);
	
	public boolean hasProperty(IPropertyType<?> prop);
	
	public <T> void setProperty(IPropertyType<T> prop, T value);
}