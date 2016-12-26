package betterquesting.api.questing.party;

import java.util.List;
import java.util.UUID;
import betterquesting.api.misc.IDataSync;
import betterquesting.api.misc.IJsonSaveLoad;
import betterquesting.api.storage.IRegStorageBase;
import com.google.gson.JsonArray;

public interface IPartyDatabase extends IRegStorageBase<Integer,IParty>, IJsonSaveLoad<JsonArray>, IDataSync
{
	public IParty getUserParty(UUID uuid);
	
	public List<Integer> getPartyInvites(UUID uuid);
}
