package betterquesting.api.questing.party;

import betterquesting.api.misc.IDataSync;
import betterquesting.api2.storage.IDatabase;
import betterquesting.api2.storage.INBTProgress;
import net.minecraft.nbt.NBTTagList;

import java.util.List;
import java.util.UUID;

public interface IPartyDatabase extends IDatabase<IParty>, INBTProgress<NBTTagList>, IDataSync
{
	IParty getUserParty(UUID uuid);
	
	List<Integer> getPartyInvites(UUID uuid);
}
