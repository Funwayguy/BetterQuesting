package betterquesting.api.party;

import java.util.UUID;
import com.google.gson.JsonObject;

public interface IPartyMember
{
	public UUID getMemberID();
	
	public String getName();
	public int getPrivilage();
	
	public JsonObject writeToJson(JsonObject json);
	public void readFromJson(JsonObject json);
}
