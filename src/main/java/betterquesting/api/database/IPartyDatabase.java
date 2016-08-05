package betterquesting.api.database;

import java.util.List;
import java.util.UUID;
import net.minecraft.entity.player.EntityPlayerMP;
import com.google.gson.JsonObject;
import betterquesting.api.party.IParty;

public interface IPartyDatabase
{
	public int getUniqueID();
	
	public void addParty(IParty party);
	public void disbandParty(int id);
	
	public IParty getParty(int partyId);
	public IParty getParty(UUID uuid);
	public List<IParty> getAllParties();
	
	public void syncAll();
	public void syncPlayer(EntityPlayerMP player);
	
	public JsonObject writeToJson(JsonObject json);
	public void readFromJson(JsonObject json);
}
