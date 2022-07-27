package betterquesting.api2.storage;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.nbt.NBTBase;

// Used when the base data set can safely be split by user. Can be used in place of INBTSaveLoad
public interface INBTPartial<T extends NBTBase, K> {
    T writeToNBT(T nbt, @Nullable List<K> subset);

    void readFromNBT(T nbt, boolean merge);
}
