package betterquesting.api2.storage;

import net.minecraft.nbt.NBTBase;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

// Used when progress specific data is being handled (usually split per user)
public interface INBTProgress<T extends NBTBase, K>
{
    T writeProgressToNBT(T nbt, @Nullable UUID users, @Nullable List<K> subset);
    void readProgressFromNBT(T nbt, boolean merge);
}
