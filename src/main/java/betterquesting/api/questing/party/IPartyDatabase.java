package betterquesting.api.questing.party;

import java.util.List;
import java.util.UUID;
import betterquesting.api.other.IDataSync;
import betterquesting.api.other.IJsonSaveLoad;
import betterquesting.api.registry.IRegStorageBase;
import com.google.gson.JsonArray;

public interface IPartyDatabase extends IRegStorageBase<Integer,IParty>, IJsonSaveLoad<JsonArray>, IDataSync
{
	public IParty getUserParty(UUID uuid);
	
	public List<Integer> getPartyInvites(UUID uuid);
}
