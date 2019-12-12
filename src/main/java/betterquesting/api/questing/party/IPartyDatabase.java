package betterquesting.api.questing.party;

import betterquesting.api2.storage.DBEntry;
import betterquesting.api2.storage.IDatabase;
import betterquesting.api2.storage.INBTPartial;
import net.minecraft.nbt.ListNBT;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;

public interface IPartyDatabase extends IDatabase<IParty>, INBTPartial<ListNBT, Integer>
{
    IParty createNew(int id);
    
    @Deprecated
	IParty getUserParty(@Nonnull UUID uuid);
    @Nullable
    DBEntry<IParty> getParty(@Nonnull UUID uuid);
}
