package betterquesting.api.misc;

import net.minecraft.nbt.NBTBase;
import betterquesting.api.enums.EnumSaveType;

public interface INBTSaveLoad<T extends NBTBase>
{
	public T writeToNBT(T nbt, EnumSaveType saveType);
	public void readFromNBT(T nbt, EnumSaveType saveType);
}
