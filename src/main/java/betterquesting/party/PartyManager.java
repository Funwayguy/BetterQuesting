package betterquesting.party;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.UUID;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import org.apache.logging.log4j.Level;
import betterquesting.core.BetterQuesting;
import betterquesting.lives.LifeManager;
import betterquesting.network.PacketAssembly;
import betterquesting.network.PacketTypeRegistry.BQPacketType;
import betterquesting.party.PartyInstance.PartyMember;
import betterquesting.utils.JsonHelper;
import betterquesting.utils.NBTConverter;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mojang.authlib.GameProfile;

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
		int pl = LifeManager.getLives(player);
		boolean flag = CreateParty(player.getUniqueID(), name);
		
		if(flag)
		{
			PartyInstance p = GetParty(player.getUniqueID());
			p.lives = Math.min(pl, p.lives);
		}
		
		return flag;
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
		
		PartyInstance p = new PartyInstance();
		p.name = name;
		
		if(!p.JoinParty(host))
		{
			return false;
		}
		
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
		PartyInstance tmp = partyList.get(party.name);
		if(tmp != null && tmp != party)
		{
			BetterQuesting.logger.log(Level.WARN, "Another party has the name '" + party.name + "'! Adjusting...");
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
	 * Scans through all players and updates the name listing
	 */
	public static void UpdateNameCache(boolean doUpdate)
	{
		boolean changed = false;
		
		MinecraftServer server = MinecraftServer.getServer();
		
		if(server != null && server.isServerRunning())
		{
			// Using the server's user cache for offline players
			for(String name : server.func_152358_ax().func_152654_a())
			{
				GameProfile prof = server.func_152358_ax().func_152655_a(name);
				
				if(prof != null)
				{
					if(!name.equalsIgnoreCase(nameCache.get(prof.getId())))
					{
						nameCache.put(prof.getId(), name);
						changed = true;
					}
				}
			}
		}
		
		if(doUpdate && changed)
		{
			UpdateClients();
		}
	}
	
	/**
	 * Gets players user name from the server or cached copy
	 */
	public static String GetUsername(UUID uuid)
	{
		String name = null;
		
		if(nameCache.containsKey(uuid))
		{
			name = nameCache.get(uuid);
		}
		
		MinecraftServer server = MinecraftServer.getServer();
		
		if(server != null && server.isServerRunning())
		{
			GameProfile prof = server.func_152358_ax().func_152652_a(uuid);
			
			if(prof != null && prof.getName() != null)
			{
				boolean update = !prof.getName().equalsIgnoreCase(name); // Casing isn't that important
				
				name = prof.getName();
				
				if(update)
				{
					nameCache.put(uuid, name);
					UpdateClients(); // Update all client's with the new UUID name
				}
			}
		}
		
		return name != null? name : uuid.toString();
	}
	
	public static void ManualUserCache(EntityPlayer player)
	{
		nameCache.put(player.getUniqueID(), player.getCommandSenderName());
	}
	
	public static void SendDatabase(EntityPlayerMP player)
	{
		NBTTagCompound tags = new NBTTagCompound();
		JsonObject json = new JsonObject();
		writeToJson(json);
		tags.setTag("Parties", NBTConverter.JSONtoNBT_Object(json, new NBTTagCompound()));
		PacketAssembly.SendTo(BQPacketType.PARTY_DATABASE.GetLocation(), tags, player);
	}
	
	public static void UpdateClients()
	{
		NBTTagCompound tags = new NBTTagCompound();
		JsonObject json = new JsonObject();
		writeToJson(json);
		tags.setTag("Parties", NBTConverter.JSONtoNBT_Object(json, new NBTTagCompound()));
		PacketAssembly.SendToAll(BQPacketType.PARTY_DATABASE.GetLocation(), tags);
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
			
			PartyInstance party = new PartyInstance();
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
