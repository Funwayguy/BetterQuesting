package betterquesting.api.questing.party;

import betterquesting.api2.storage.DBEntry;
import betterquesting.api2.storage.IDatabase;
import betterquesting.api2.storage.INBTPartial;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.nbt.NBTTagList;

public interface IPartyDatabase extends IDatabase<IParty>, INBTPartial<NBTTagList, Integer> {
    IParty createNew(int id);

    @Nullable
    DBEntry<IParty> getParty(@Nonnull UUID uuid);
}
