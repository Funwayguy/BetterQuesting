package betterquesting.api.misc;

import net.minecraft.nbt.NBTBase;
import betterquesting.api.enums.EnumSaveType;

public interface INBTSaveLoad<T extends NBTBase>
{
	T writeToNBT(T nbt, EnumSaveType saveType);
	void readFromNBT(T nbt, EnumSaveType saveType);
}
