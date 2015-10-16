package betterquesting.party;

import java.util.ArrayList;
import java.util.UUID;
import org.apache.logging.log4j.Level;
import betterquesting.core.BetterQuesting;
import betterquesting.utils.JsonHelper;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

public class PartyInstance
{
	String name = "New Party";
	UUID host;
	ArrayList<UUID> members = new ArrayList<UUID>();
	ArrayList<UUID> invites = new ArrayList<UUID>();
	
	public PartyInstance(String name, UUID host)
	{
		this.name = name;
		this.host = host;
		
		members.add(host);
	}
	
	public boolean isInParty(UUID uuid)
	{
		return members.contains(uuid);
	}
	
	public void InvitePlayer(UUID uuid)
	{
		if(!invites.contains(uuid))
		{
			invites.add(uuid);
		}
	}
	
	public ArrayList<UUID> GetMembers()
	{
		return members;
	}
	
	/**
	 * Adds a player to the given party. They must have been previously invited
	 * @param player
	 * @return true if the player was added successfully
	 */
	public boolean JoinParty(UUID uuid)
	{
		if(PartyManager.GetParty(uuid) != null)
		{
			return false; // Already part of another party
		} else if(invites.contains(uuid) && !members.contains(uuid))
		{
			invites.remove(uuid);
			return members.add(uuid);
		} else
		{
			return false;
		}
	}
	
	/**
	 * Purges this player from both the party members and invite list
	 * @param player
	 */
	public void LeaveParty(UUID uuid)
	{
		invites.remove(uuid);
		members.remove(uuid);
		
		if(members.size() <= 0 || this.host.equals(uuid)) // Cannot have a party without a host or no members so we disband this party
		{
			PartyManager.Disband(this.name);
		}
	}
	
	public void writeToJson(JsonObject jObj)
	{
		jObj.addProperty("name", this.name);
		jObj.addProperty("host", this.host.toString());
		
		JsonArray memJson = new JsonArray();
		
		for(UUID uuid : members)
		{
			memJson.add(new JsonPrimitive(uuid.toString()));
		}
		
		jObj.add("members", memJson);
		
		JsonArray invJson = new JsonArray();
		
		for(UUID uuid : invites)
		{
			invJson.add(new JsonPrimitive(uuid.toString()));
		}
		
		jObj.add("invites", invJson);
	}
	
	public void readFromJson(JsonObject jObj)
	{
		// These are read out before instantiation by the party manager
		//this.name = jObj.get("name").getAsString();
		//this.host = UUID.fromString(jObj.get("host").getAsString());
		
		invites.clear();
		for(JsonElement entry : JsonHelper.GetArray(jObj, "memebers"))
		{
			if(entry == null || !entry.isJsonPrimitive() || !entry.getAsJsonPrimitive().isString())
			{
				continue;
			}
			
			try
			{
				invites.add(UUID.fromString(entry.getAsString()));
			} catch(Exception e)
			{
				BetterQuesting.logger.log(Level.ERROR, "Failed to load party member for party '" + this.name + "'");
			}
		}
		
		members.clear();
		for(JsonElement entry : JsonHelper.GetArray(jObj, "invites"))
		{
			if(entry == null || !entry.isJsonPrimitive() || !entry.getAsJsonPrimitive().isString())
			{
				continue;
			}
			
			try
			{
				members.add(UUID.fromString(entry.getAsString()));
			} catch(Exception e)
			{
				BetterQuesting.logger.log(Level.ERROR, "Failed to load party invite for party '" + this.name + "'");
			}
		}
	}
}
