package betterquesting.api2.storage;

import net.minecraft.nbt.NBTBase;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

// Used when the base data set can safely be split by user. Can be used in place of INBTSaveLoad
public interface INBTPartial<T extends NBTBase>
{
    T writeToNBT(T nbt, @Nullable List<UUID> users);
    void readFromNBT(T nbt, boolean merge);
}
