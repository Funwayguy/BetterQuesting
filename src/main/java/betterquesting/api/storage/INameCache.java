package betterquesting.api.storage;

import betterquesting.api2.storage.INBTPartial;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nonnull;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagList;

public interface INameCache extends INBTPartial<NBTTagList, UUID> {
    boolean updateName(@Nonnull EntityPlayerMP player);

    String getName(@Nonnull UUID uuid);

    UUID getUUID(@Nonnull String name);

    List<String> getAllNames();

    /**
     * Used primarily to know if a user is an OP client side<br>
     */
    boolean isOP(UUID uuid);

    int size();

    void reset();
}
