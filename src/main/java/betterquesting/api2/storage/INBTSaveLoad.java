package betterquesting.api2.storage;

import net.minecraft.nbt.NBTBase;

// TODO: Replace usage with INBTSerializable?
public interface INBTSaveLoad<T extends NBTBase> {
    T writeToNBT(T nbt);

    void readFromNBT(T nbt);
}
