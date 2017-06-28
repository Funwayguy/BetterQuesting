package adv_director.api.storage;

import java.util.UUID;
import adv_director.api.misc.IDataSync;
import adv_director.api.misc.IJsonSaveLoad;
import adv_director.api.questing.party.IParty;
import com.google.gson.JsonObject;

public interface ILifeDatabase extends IJsonSaveLoad<JsonObject>, IDataSync
{
	@Deprecated
	public int getDefaultLives();
	@Deprecated
	public int getMaxLives();
	
	public int getLives(UUID uuid);
	public void setLives(UUID uuid, int value);
	
	public int getLives(IParty party);
	public void setLives(IParty party, int value);
}
