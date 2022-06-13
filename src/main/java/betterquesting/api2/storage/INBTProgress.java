package betterquesting.api2.storage;

import net.minecraft.nbt.NBTBase;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

/**
 * Used when progress specific data is being handled (usually split per user)
 */
public interface INBTProgress<T extends NBTBase> {

    /**
     *
     * @param nbt the nbt tag to write progress to
     * @param users the users to write to the nbt tag, if null all users should be written
     * @return the nbt tag written
     */
    T writeProgressToNBT(T nbt, @Nullable List<UUID> users);

    /**
     * @param nbt the nbt tag to read from
     * @param merge if true, the progress for some users will be merged with the existing progress, otherwise it will be overwritten
     */
    void readProgressFromNBT(T nbt, boolean merge);
}
