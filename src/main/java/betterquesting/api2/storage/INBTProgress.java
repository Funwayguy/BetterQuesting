package betterquesting.api2.storage;

import java.util.List;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.nbt.NBTBase;

// Used when progress specific data is being handled (usually split per user)
public interface INBTProgress<T extends NBTBase> {
    /** If users is not null, only the progress for the users in the list will be written to the NBT */
    T writeProgressToNBT(T nbt, @Nullable List<UUID> users);
    /** if merge is true, the progress for some users will be merged with the existing progress, otherwise it will be overwritten */
    void readProgressFromNBT(T nbt, boolean merge);
}
