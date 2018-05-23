package betterquesting.api2.storage;

import betterquesting.api.misc.INBTSaveLoad;
import net.minecraft.nbt.NBTBase;

public interface IDatabaseNBT<T, K extends NBTBase> extends IDatabase<T>, INBTSaveLoad<K>
{
}
