package betterquesting.api2.storage;

import net.minecraft.nbt.INBT;

import javax.annotation.Nullable;
import java.util.List;

// Used when the base data set can safely be split. Can be used in place of INBTSaveLoad
public interface INBTPartial<T extends INBT, K>
{
    T writeToNBT(T nbt, @Nullable List<K> subset);
    void readFromNBT(T nbt, boolean merge);
}
