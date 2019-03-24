package betterquesting.misc;

import net.minecraft.nbt.NBTTagCompound;

import javax.annotation.Nonnull;
import java.util.UUID;

// Just use an NBT instance in a HashMap >_>
@Deprecated
public class UserEntry
{
	private final UUID uuid;
	
	private NBTTagCompound tags = new NBTTagCompound();
	
    @Deprecated
	public UserEntry(UUID uuid, long timestamp)
	{
		this(uuid);
		this.tags.setLong("timestamp", timestamp);
	}
	
	public UserEntry(UUID uuid)
	{
		this.uuid = uuid;
	}
	
	public UserEntry(NBTTagCompound nbt)
    {
        uuid = UUID.fromString(nbt.getString("uuid"));
        this.readFromJson(nbt);
    }
	
	public UUID getUUID()
	{
		return uuid;
	}
	
	@Nonnull
	public NBTTagCompound getNbtData()
    {
        return tags;
    }
    
    /**
     * Deprecated: Retrieve the data directly from getNbtData()
     * @param state
     * @param time
     */
    @Deprecated
	public void setClaimed(boolean state, long time)
	{
	    tags.setBoolean("claimed", state);
	    tags.setLong("timestamp", time);
	}
    
    /**
     * Deprecated: Retrieve the data directly from getNbtData()
     * @return Timestamp of the last time the user claimed their reward(s)
     */
	@Deprecated
	public long getTimestamp()
	{
		return tags.getLong("timestamp");
	}
    
    /**
     * Deprecated: Retrieve the data directly from getNbtData()
     * @return Whether or not the player has claimed their quest reward(s)
     */
	@Deprecated
	public boolean hasClaimed()
	{
		return tags.getBoolean("claimed");
	}
	
	public NBTTagCompound writeToJson(NBTTagCompound nbt)
	{
	    nbt.merge(tags);
	    nbt.setString("uuid", uuid.toString()); // More of a legacy thing but also to enforce the original UUID
		return nbt;
	}
	
	public void readFromJson(NBTTagCompound nbt)
	{
	    tags = new NBTTagCompound();
	    tags.merge(nbt);
	    tags.removeTag("uuid"); // Remove unsafe UUID reference
	}
}
