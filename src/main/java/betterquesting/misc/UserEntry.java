package betterquesting.misc;

import java.util.UUID;
import net.minecraft.nbt.NBTTagCompound;

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
	
	public NBTTagCompound writeToJson(NBTTagCompound json)
	{
		json.setString("uuid", uuid.toString());
		json.setLong("timestamp", timestamp);
		json.setBoolean("claimed", claimed);
		return json;
	}
	
	public void readFromJson(NBTTagCompound json)
	{
		uuid = UUID.fromString(json.getString("uuid"));
		timestamp = json.getLong("timestamp");
		claimed = json.getBoolean("claimed");
	}
}
