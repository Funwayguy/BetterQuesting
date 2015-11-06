package betterquesting.party;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import org.apache.logging.log4j.Level;
import betterquesting.core.BetterQuesting;
import betterquesting.network.PacketQuesting;
import betterquesting.party.PartyInstance.PartyMember;
import betterquesting.utils.JsonHelper;
import betterquesting.utils.NBTConverter;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class PartyManager
{
	public static boolean updateUI = true;
	static HashMap<String, PartyInstance> partyList = new HashMap<String, PartyInstance>();
	static HashMap<UUID, String> nameCache = new HashMap<UUID, String>(); // Used for display purposes only
	
	/**
	 * Creates a new party with the given player as host and adds it to the list of active parties
	 * @param player
	 * @return
	 */
	public static boolean CreateParty(EntityPlayer player, String name)
	{
		nameCache.put(player.getUniqueID(), player.getCommandSenderName());
		if(partyList.containsKey(name) || GetParty(player.getUniqueID()) != null)
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
	
	public static PartyInstance GetPartyByName(String name)
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
			PartyMember mem = party.GetMemberData(player.getUniqueID());
			
			if(mem != null && mem.GetPrivilege() == 0)
			{
				list.add(party);
			}
		}
		
		return list;
	}
	
	public static PartyInstance GetParty(UUID uuid)
	{
		for(PartyInstance party : partyList.values())
		{
			if(party.members.contains(uuid))
			{
				return party;
			}
		}
		
		return null;
	}
	
	public static void SendDatabase(EntityPlayerMP player)
	{
		NBTTagCompound tags = new NBTTagCompound();
		tags.setInteger("ID", 2);
		JsonObject json = new JsonObject();
		writeToJson(json);
		tags.setTag("Parties", NBTConverter.JSONtoNBT_Object(json, new NBTTagCompound()));
		BetterQuesting.instance.network.sendTo(new PacketQuesting(tags), player);
	}
	
	public static void UpdateClients()
	{
		NBTTagCompound tags = new NBTTagCompound();
		tags.setInteger("ID", 2);
		JsonObject json = new JsonObject();
		writeToJson(json);
		tags.setTag("Parties", NBTConverter.JSONtoNBT_Object(json, new NBTTagCompound()));
		BetterQuesting.instance.network.sendToAll(new PacketQuesting(tags));
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
	
	public static void readFromJson(JsonObject json)
	{
		if(json == null)
		{
			json = new JsonObject();
		}
		
		updateUI = true;
		partyList.clear();
		
		for(JsonElement entry : JsonHelper.GetArray(json, "partyList"))
		{
			if(entry == null || !entry.isJsonObject())
			{
				continue;
			}
			
			try
			{
				UUID host = UUID.fromString(JsonHelper.GetString(entry.getAsJsonObject(), "host", ""));
				String name = JsonHelper.GetString(entry.getAsJsonObject(), "name", "");
				
				if(name == null || name.length() <= 0)
				{
					BetterQuesting.logger.log(Level.WARN, "Tried to load party with invalid name! Skipping...");
					continue;
				}
				
				PartyInstance party = new PartyInstance(name, host);
				partyList.put(party.name, party);
			} catch(Exception e)
			{
				BetterQuesting.logger.log(Level.ERROR, "Unable to load party instance", e);
			}
		}
	}
}
