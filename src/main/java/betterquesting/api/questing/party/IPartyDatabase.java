package betterquesting.api.questing.party;

import java.util.List;
import java.util.UUID;
import net.minecraft.nbt.NBTTagList;
import betterquesting.api.misc.IDataSync;
import betterquesting.api.misc.INBTSaveLoad;
import betterquesting.api.storage.IRegStorageBase;

public interface IPartyDatabase extends IRegStorageBase<Integer,IParty>, INBTSaveLoad<NBTTagList>, IDataSync
{
	public IParty getUserParty(UUID uuid);
	
	public List<Integer> getPartyInvites(UUID uuid);
}
