package betterquesting.party;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;
import net.minecraft.entity.player.EntityPlayer;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class PartyManager
{
	static HashMap<String, PartyInstance> partyList = new HashMap<String, PartyInstance>();
	
	/**
	 * Creates a new party with the given player as host and adds it to the list of active parties
	 * @param player
	 * @return
	 */
	public static boolean CreateParty(EntityPlayer player, String name)
	{
		if(partyList.containsKey(name))
		{
			return false;
		}
		
		partyList.put(name, new PartyInstance(name, player.getUniqueID()));
		return true;
	}
	
	/**
	 * Deletes the party with the given name
	 * @param name
	 */
	public static void Disband(String name)
	{
		partyList.remove(name);
	}
	
	public static PartyInstance GetParty(String name)
	{
		return partyList.get(name);
	}
	
	/**
	 * Returns a list of party invites this player has
	 * @param player
	 * @return
	 */
	public static ArrayList<PartyInstance> getInvites(EntityPlayer player)
	{
		ArrayList<PartyInstance> list = new ArrayList<PartyInstance>();
		
		for(PartyInstance party : partyList.values())
		{
			if(party.invites.contains(player.getUniqueID()))
			{
				list.add(party);
			}
		}
		
		return list;
	}
	
	public static ArrayList<PartyInstance> GetParties(UUID uuid)
	{
		ArrayList<PartyInstance> list = new ArrayList<PartyInstance>();
		
		for(PartyInstance party : partyList.values())
		{
			if(party.members.contains(uuid))
			{
				list.add(party);
			}
		}
		
		return list;
	}
	
	public static void writeToJson(JsonObject jObj)
	{
		JsonArray ptyJson = new JsonArray();
		
		for(PartyInstance party : partyList.values())
		{
			if(party != null)
			{
				JsonObject partyJson = new JsonObject();
				party.writeToJson(partyJson);
				ptyJson.add(partyJson);
			}
		}
		
		jObj.add("partyList", ptyJson);
	}
	
	public static void readFromJson(JsonObject jObj)
	{
		partyList.clear();
		
		for(JsonElement entry : jObj.getAsJsonArray("partyList"))
		{
			PartyInstance party = new PartyInstance(entry.getAsJsonObject());
			partyList.put(party.name, party);
		}
	}
}
