package betterquesting.api.party;

import java.util.List;
import java.util.UUID;
import com.google.gson.JsonObject;

public interface IParty
{
	public int getPartyID();
	
	public String getName();
	public IPartyMember getHost();
	
	public boolean shareLives();
	public boolean shareReward();
	
	public void joinMember(UUID uuid);
	public void kickMember(UUID uuid);
	
	public List<IPartyMember> getMembers();
	
	public JsonObject writeToJson(JsonObject json);
	public void readFromJson(JsonObject json);
}
