package adv_director.rw2.api.utils;

import net.minecraft.nbt.NBTBase;

public interface INbtSaveLoad<T extends NBTBase>
{
	public T writeToNBT(T nbt);
	public void readFromNBT(T nbt);
}
