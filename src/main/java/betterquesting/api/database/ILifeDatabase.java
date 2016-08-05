package betterquesting.api.database;

import java.util.UUID;
import net.minecraft.entity.player.EntityPlayerMP;
import com.google.gson.JsonObject;
import betterquesting.api.party.IParty;

public interface ILifeDatabase
{
	public int getLives(UUID uuid);
	public void setLives(UUID uuid, int value);
	
	public int getLives(IParty party);
	public void setLives(IParty party, int value);
	
	public void syncAll();
	public void syncPlayer(EntityPlayerMP player);
	
	public JsonObject writeToJson(JsonObject json);
	public void readFromJson(JsonObject json);
}
