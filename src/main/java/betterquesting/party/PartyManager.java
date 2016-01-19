package betterquesting.party;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.UUID;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.WorldServer;
import org.apache.logging.log4j.Level;
import betterquesting.core.BetterQuesting;
import betterquesting.network.PacketQuesting.PacketDataType;
import betterquesting.party.PartyInstance.PartyMember;
import betterquesting.utils.JsonHelper;
import betterquesting.utils.NBTConverter;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

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
		return CreateParty(player.getUniqueID(), name);
	}
	
	/**
	 * Creates a new party with the given player as host and adds it to the list of active parties
	 * @param player
	 * @return
	 */
	public static boolean CreateParty(UUID host, String name)
	{
		if(partyList.containsKey(name) || GetParty(host) != null)
		{
			return false;
		}
		
		PartyInstance p = new PartyInstance(name);
		p.JoinParty(host);
		partyList.put(name, p);
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
	 * Updates the party name mapping
	 */
	public static void ApplyNameChange(PartyInstance party)
	{
		if(partyList.get(party.name) != party)
		{
			BetterQuesting.logger.log(Level.ERROR, "");
			int i = 0;
			while(partyList.containsKey(party.name + " #" + i))
			{
				i++;
			}
			party.name = party.name + " #" + i;
		}
		
		partyList.remove(party);
		partyList.put(party.name, party);
	}
	
	/**
	 * Returns a list of party invites this player has
	 * @param player
	 * @return
	 */
	public static ArrayList<PartyInstance> getInvites(UUID uuid)
	{
		ArrayList<PartyInstance> list = new ArrayList<PartyInstance>();
		
		for(PartyInstance party : partyList.values())
		{
			PartyMember mem = party.GetMemberData(uuid);
			
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
			for(PartyMember mem : party.members)
			{
				if(mem.userID.equals(uuid) && mem.GetPrivilege() > 0)
				{
					return party;
				}
			}
		}
		
		return null;
	}
	
	/**
	 * Gets players user name from the server or cached copy
	 */
	public static String GetUsername(UUID uuid)
	{
		MinecraftServer server = MinecraftServer.getServer();
		
		if(server != null && server.isServerRunning())
		{
			for(WorldServer world : server.worldServers)
			{
				EntityPlayer player = world.func_152378_a(uuid);
				
				if(player != null)
				{
					nameCache.put(uuid, player.getCommandSenderName());
					return player.getCommandSenderName();
				}
			}
		}
		
		if(nameCache.containsKey(uuid))
		{
			return nameCache.get(uuid);
		}
		
		return uuid.toString();
	}
	
	public static void SendDatabase(EntityPlayerMP player)
	{
		NBTTagCompound tags = new NBTTagCompound();
		//tags.setInteger("ID", 2);
		JsonObject json = new JsonObject();
		writeToJson(json);
		tags.setTag("Parties", NBTConverter.JSONtoNBT_Object(json, new NBTTagCompound()));
		//BetterQuesting.instance.network.sendTo(new PacketQuesting(tags), player);
		BetterQuesting.instance.network.sendTo(PacketDataType.PARTY_DATABASE.makePacket(tags), player);
	}
	
	public static void UpdateClients()
	{
		NBTTagCompound tags = new NBTTagCompound();
		//tags.setInteger("ID", 2);
		JsonObject json = new JsonObject();
		writeToJson(json);
		tags.setTag("Parties", NBTConverter.JSONtoNBT_Object(json, new NBTTagCompound()));
		//BetterQuesting.instance.network.sendToAll(new PacketQuesting(tags));
		BetterQuesting.instance.network.sendToAll(PacketDataType.PARTY_DATABASE.makePacket(tags));
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
		
		JsonObject cache = new JsonObject();
		
		for(Entry<UUID,String> entry : nameCache.entrySet())
		{
			cache.addProperty(entry.getKey().toString(), entry.getValue());
		}
		
		jObj.add("nameCache", cache);
	}
	
	public static void readFromJson(JsonObject json)
	{
		if(json == null)
		{
			json = new JsonObject();
		}
		
		updateUI = true;
		partyList = new HashMap<String, PartyInstance>();
		
		for(JsonElement entry : JsonHelper.GetArray(json, "partyList"))
		{
			if(entry == null || !entry.isJsonObject())
			{
				continue;
			}
			
			String name = JsonHelper.GetString(entry.getAsJsonObject(), "name", "");
			
			if(name == null || name.length() <= 0)
			{
				BetterQuesting.logger.log(Level.WARN, "Tried to load party with invalid name! Skipping...");
				continue;
			}
			
			PartyInstance party = new PartyInstance(name);
			party.readFromJson(entry.getAsJsonObject());
			partyList.put(party.name, party);
		}
		
		nameCache = new HashMap<UUID, String>();
		
		for(Entry<String,JsonElement> entry : JsonHelper.GetObject(json, "nameCache").entrySet())
		{
			if(entry == null || entry.getValue() == null || !(entry.getValue() instanceof JsonPrimitive))
			{
				continue;
			}
			
			UUID uuid;
			
			try
			{
				uuid = UUID.fromString(entry.getKey());
			} catch(Exception e)
			{
				BetterQuesting.logger.log(Level.ERROR, "Unable to read UUID from name cache", e);
				continue;
			}
			
			nameCache.put(uuid, entry.getValue().getAsString());
		}
	}
}
