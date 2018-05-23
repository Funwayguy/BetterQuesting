package betterquesting.api.questing.party;

import java.util.List;
import java.util.UUID;

import betterquesting.api2.storage.IDatabaseNBT;
import net.minecraft.nbt.NBTTagList;
import betterquesting.api.misc.IDataSync;

public interface IPartyDatabase extends IDatabaseNBT<IParty, NBTTagList>, IDataSync
{
	IParty getUserParty(UUID uuid);
	
	List<Integer> getPartyInvites(UUID uuid);
}
