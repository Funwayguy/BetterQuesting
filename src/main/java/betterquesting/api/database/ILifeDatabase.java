package betterquesting.api.database;

import java.util.UUID;
import betterquesting.api.party.IParty;
import com.google.gson.JsonObject;

public interface ILifeDatabase extends IJsonSaveLoad<JsonObject>, IDataSync
{
	public int getDefaultLives();
	public int getMaxLives();
	
	public int getLives(UUID uuid);
	public void setLives(UUID uuid, int value);
	
	public int getLives(IParty party);
	public void setLives(IParty party, int value);
}
