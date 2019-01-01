package betterquesting.api2.storage;

import net.minecraft.nbt.NBTBase;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

public interface INBTProgress<T extends NBTBase>
{
    T writeProgressToNBT(T nbt, @Nullable List<UUID> users);
    void readProgressFromNBT(T nbt, boolean merge);
}
