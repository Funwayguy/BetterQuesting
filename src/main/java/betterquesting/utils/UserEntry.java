package betterquesting.utils;

import java.util.UUID;
import betterquesting.api.utils.JsonHelper;
import com.google.gson.JsonObject;

public class UserEntry
{
	private UUID uuid;
	private long timestamp = 0;
	private boolean claimed = false;
	
	public UserEntry(UUID uuid, long timestamp)
	{
		this(uuid);
		this.timestamp = timestamp;
	}
	
	public UserEntry(UUID uuid)
	{
		this.uuid = uuid;
	}
	
	public void setClaimed(boolean state, long time)
	{
		this.claimed = state;
		this.timestamp = time;
	}
	
	public UUID getUUID()
	{
		return uuid;
	}
	
	public long getTimestamp()
	{
		return timestamp;
	}
	
	public boolean hasClaimed()
	{
		return claimed;
	}
	
	public JsonObject writeToJson(JsonObject json)
	{
		json.addProperty("uuid", uuid.toString());
		json.addProperty("timestamp", timestamp);
		json.addProperty("claimed", claimed);
		return json;
	}
	
	public void readFromJson(JsonObject json)
	{
		uuid = UUID.fromString(JsonHelper.GetString(json, "uuid", ""));
		timestamp = JsonHelper.GetNumber(json, "timestamp", 0).longValue();
		claimed = JsonHelper.GetBoolean(json, "claimed", false);
	}
}
