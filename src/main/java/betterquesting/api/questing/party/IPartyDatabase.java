package betterquesting.api.questing.party;

import betterquesting.api2.storage.IDatabase;
import betterquesting.api2.storage.INBTPartial;
import net.minecraft.nbt.NBTTagList;

import java.util.List;
import java.util.UUID;

public interface IPartyDatabase extends IDatabase<IParty>, INBTPartial<NBTTagList, Integer>
{
	IParty getUserParty(UUID uuid);
	
	List<Integer> getPartyInvites(UUID uuid);
}
