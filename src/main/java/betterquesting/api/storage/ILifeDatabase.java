package betterquesting.api.storage;

import betterquesting.api2.storage.INBTPartial;
import net.minecraft.nbt.NBTTagCompound;

import javax.annotation.Nonnull;
import java.util.UUID;

public interface ILifeDatabase extends INBTPartial<NBTTagCompound, UUID> {
    int getLives(@Nonnull UUID uuid);

    void setLives(@Nonnull UUID uuid, int value);

    void reset();
}
