package betterquesting.api.misc;

import betterquesting.api.enums.EnumSaveType;
import net.minecraft.nbt.NBTBase;

public interface INBTSaveLoad<T extends NBTBase>
{
	T writeToNBT(T nbt, EnumSaveType saveType);
	void readFromNBT(T nbt, EnumSaveType saveType);
}
