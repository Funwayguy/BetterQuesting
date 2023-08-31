package betterquesting.api2.storage;

import net.minecraft.nbt.NBTBase;

public interface IDatabaseNBT<T, E extends NBTBase, K extends NBTBase>
    extends IDatabase<T>, INBTPartial<E, Integer>, INBTProgress<K> {
}
