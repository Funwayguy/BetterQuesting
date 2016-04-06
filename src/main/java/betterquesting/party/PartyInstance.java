package betterquesting.party;

import java.util.ArrayList;
import java.util.UUID;
import org.apache.logging.log4j.Level;
import betterquesting.core.BetterQuesting;
import betterquesting.lives.LifeManager;
import betterquesting.utils.JsonHelper;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class PartyInstance
{
	public String name = "New Party";
	ArrayList<PartyMember> members = new ArrayList<PartyMember>();
	
	public int lives = 1;
	public boolean lifeShare = false;
	public boolean lootShare = false;
	
	public PartyInstance()
	{
		lives = LifeManager.defLives;
	}
	
	public void InvitePlayer(UUID uuid)
	{
		PartyMember mem = GetMemberData(uuid);
		
		if(mem == null)
		{
			mem = new PartyMember(uuid);
			mem.privilege = 0;
			members.add(mem);
		}
	}
	
	public ArrayList<PartyMember> GetMembers()
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
		PartyMember mem = GetMemberData(uuid);
		
		if(mem != null && mem.privilege == 0)
		{
			mem.privilege = 1;
			return true;
		} else if(members.size() <= 0)
		{
			mem = new PartyMember(uuid);
			mem.privilege = 2;
			members.add(mem);
			return true;
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
		PartyMember mem = GetMemberData(uuid);
		
		if(mem == null)
		{
			return;
		}
		
		members.remove(mem);
		
		if(members.size() <= 0) // Cannot have a party without any members so we disband this party
		{
			PartyManager.Disband(name);
		} else if(mem.privilege >= 2) // Try host migration
		{
			PartyMember nHost = null;
			
			for(PartyMember m : members)
			{
				if(m.privilege == 2) // There's another host?
				{
					return;
				} else if(m.privilege == 1 && nHost != null) // Only members that have previously accepted their invites can be hosts
				{
					nHost = m; // We don't break in case there is another host
				}
			}
			
			if(nHost == null)
			{
				PartyManager.Disband(name);
			} else
			{
				nHost.privilege = 2;
			}
		}
	}
	
	public boolean isHost(UUID uuid)
	{
		PartyMember mem = GetMemberData(uuid);
		
		return mem != null && mem.privilege >= 2;
	}
	
	/**
	 * @param uuid
	 * @return Party membership data or null if player is not a member of this party
	 */
	public PartyMember GetMemberData(UUID uuid)
	{
		for(PartyMember mem : members)
		{
			if(mem.userID.equals(uuid))
			{
				return mem;
			}
		}
		
		return null;
	}
	
	public void writeToJson(JsonObject jObj)
	{
		jObj.addProperty("name", name);
		jObj.addProperty("lifeShare", lifeShare);
		jObj.addProperty("lootShare", lootShare);
		jObj.addProperty("lives", lives);
		
		JsonArray memJson = new JsonArray();
		
		for(PartyMember mem : members)
		{
			memJson.add(mem.getJson());
		}
		
		jObj.add("members", memJson);
	}
	
	public void readFromJson(JsonObject jObj)
	{
		name = JsonHelper.GetString(jObj, "name", "New Party");
		lifeShare = JsonHelper.GetBoolean(jObj, "lifeShare", false);
		lootShare = JsonHelper.GetBoolean(jObj, "lootShare", false);
		lives = JsonHelper.GetNumber(jObj, "lives", 1).intValue();
		
		members = new ArrayList<PartyMember>();
		for(JsonElement entry : JsonHelper.GetArray(jObj, "members"))
		{
			if(entry == null || !entry.isJsonObject())
			{
				continue;
			}
			
			try
			{
				PartyMember pMem = new PartyMember(UUID.fromString(JsonHelper.GetString(entry.getAsJsonObject(), "userID", "")));
				pMem.readJson(entry.getAsJsonObject());
				members.add(pMem);
			} catch(Exception e)
			{
				BetterQuesting.logger.log(Level.ERROR, "Failed to load party member for party '" + this.name + "'");
			}
		}
	}
	
	public static class PartyMember
	{
		public final UUID userID;
		/**
		 * Ch-ch-check your privilege!</br>
		 * 0 = invited, 1 = member, 2 = host</br>
		 * Operators may not be able to forcibly join parties however they can kick any user without the hosts consent!
		 * This power is intentionally reserved for situations such as forcing a host migration when the user has been offline for an extended period of time. 
		 */
		private int privilege = 0; // Ch-ch-check your privilege!
		
		public PartyMember(UUID uuid)
		{
			this.userID = uuid;
		}
		/**
		 * 0 = invited, 1 = member, 2 = host</br>
		 */
		public int GetPrivilege()
		{
			return privilege;
		}
		
		public JsonObject getJson()
		{
			JsonObject json = new JsonObject();
			json.addProperty("userID", userID.toString());
			json.addProperty("privilege", privilege);
			return json;
		}
		
		public void readJson(JsonObject json)
		{
			privilege = JsonHelper.GetNumber(json, "privilege", 0).intValue();
		}
	}
}
