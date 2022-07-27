package betterquesting.api.storage;

import betterquesting.api2.storage.INBTPartial;
import java.util.UUID;
import net.minecraft.nbt.NBTTagCompound;

public interface ILifeDatabase extends INBTPartial<NBTTagCompound, UUID> {
    int getLives(UUID uuid);

    void setLives(UUID uuid, int value);

    void reset();
}
