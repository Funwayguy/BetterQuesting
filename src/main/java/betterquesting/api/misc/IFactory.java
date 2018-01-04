package betterquesting.api.misc;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

public interface IFactory<T>
{
	public ResourceLocation getRegistryName();
	
	public T createNew();
	public T loadFromNBT(NBTTagCompound json);
}
