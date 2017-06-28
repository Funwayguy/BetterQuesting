package adv_director.api.questing.party;

import java.util.List;
import java.util.UUID;
import adv_director.api.misc.IDataSync;
import adv_director.api.misc.IJsonSaveLoad;
import adv_director.api.storage.IRegStorageBase;
import com.google.gson.JsonArray;

public interface IPartyDatabase extends IRegStorageBase<Integer,IParty>, IJsonSaveLoad<JsonArray>, IDataSync
{
	public IParty getUserParty(UUID uuid);
	
	public List<Integer> getPartyInvites(UUID uuid);
}
