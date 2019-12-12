package betterquesting.api2.storage;

import net.minecraft.nbt.INBT;

public interface IDatabaseNBT<T, E extends INBT, K extends INBT> extends IDatabase<T>, INBTPartial<E, Integer>, INBTProgress<K>
{
}
