package betterquesting.api.database;

import java.util.List;
import java.util.UUID;
import betterquesting.api.party.IParty;
import com.google.gson.JsonArray;

public interface IPartyDatabase extends IRegStorage<IParty>, IJsonSaveLoad<JsonArray>, IDataSync
{
	public IParty getUserParty(UUID uuid);
	
	public List<Integer> getPartyInvites(UUID uuid);
}
