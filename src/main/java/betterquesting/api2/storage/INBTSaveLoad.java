package betterquesting.api2.storage;

import net.minecraft.nbt.NBTBase;

// General purpose NBT save/load interface where data splitting is unnecessary or impractical
public interface INBTSaveLoad<T extends NBTBase>
{
    T writeToNBT(T nbt);
    void readFromNBT(T nbt);
}
