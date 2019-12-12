package betterquesting.api2.storage;

import net.minecraft.nbt.INBT;

// TODO: Replace usage with INBTSerializable?
public interface INBTSaveLoad<T extends INBT>
{
    T writeToNBT(T nbt);
    void readFromNBT(T nbt);
}
