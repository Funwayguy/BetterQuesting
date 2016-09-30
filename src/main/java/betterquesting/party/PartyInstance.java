package betterquesting.party;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;
import net.minecraft.nbt.NBTTagCompound;
import betterquesting.api.enums.EnumPartyStatus;
import betterquesting.api.enums.EnumSaveType;
import betterquesting.api.network.PacketTypeNative;
import betterquesting.api.network.PreparedPayload;
import betterquesting.api.party.IParty;
import betterquesting.api.utils.JsonHelper;
import betterquesting.api.utils.NBTConverter;
import betterquesting.network.PacketSender;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class PartyInstance implements IParty
{
	private String name = "New Party";
	private HashMap<UUID, EnumPartyStatus> members = new HashMap<UUID, EnumPartyStatus>();
	
	private int lives = 3;
	private boolean lifeShare = false;
	private boolean lootShare = false;
	
	public PartyInstance()
	{
	}
	
	@Override
	public String getName()
	{
		return name;
	}
	
	@Override
	public void setName(String name)
	{
		this.name = name;
	}
	
	@Override
	public boolean getShareLives()
	{
		return lifeShare;
	}
	
	@Override
	public boolean getShareReward()
	{
		return lootShare;
	}
	
	@Override
	public void setShareLives(boolean state)
	{
		lifeShare = state;
	}
	
	@Override
	public void setShareReward(boolean state)
	{
		lootShare = state;
	}
	
	@Override
	public void inviteUser(UUID uuid)
	{
		if(uuid == null || members.containsKey(uuid))
		{
			return;
		}
		
		if(members.size() == 0)
		{
			members.put(uuid, EnumPartyStatus.OWNER);
		} else
		{
			members.put(uuid, EnumPartyStatus.INVITE);
		}
		
		PacketSender.INSTANCE.sendToAll(getSyncPacket());
	}
	
	@Override
	public void kickUser(UUID uuid)
	{
		if(!members.containsKey(uuid))
		{
			return;
		}
		
		EnumPartyStatus old = members.get(uuid);
		
		members.remove(uuid);
		
		if(members.size() <= 0)
		{
			PartyManager.INSTANCE.removeValue(this);
			PacketSender.INSTANCE.sendToAll(PartyManager.INSTANCE.getSyncPacket());
		} else if(old == EnumPartyStatus.OWNER)
		{
			hostMigrate();
		}
		
		PacketSender.INSTANCE.sendToAll(getSyncPacket());
	}
	
	@Override
	public void setStatus(UUID uuid, EnumPartyStatus priv)
	{
		if(!members.containsKey(uuid))
		{
			return;
		}
		
		EnumPartyStatus old = members.get(uuid);
		
		if(old == priv)
		{
			return;
		}
		
		members.put(uuid, priv);
		
		if(priv == EnumPartyStatus.OWNER)
		{
			for(UUID mem : getMembers())
			{
				if(mem != uuid && members.get(mem) == EnumPartyStatus.OWNER)
				{
					// Removes previous owner
					members.put(mem, EnumPartyStatus.ADMIN);
					break;
				}
			}
		} else if(old == EnumPartyStatus.OWNER)
		{
			UUID migrate = null;
			
			// Find new owner
			for(UUID mem : getMembers())
			{
				if(mem == uuid)
				{
					continue;
				} else if(members.get(mem) == EnumPartyStatus.ADMIN)
				{
					migrate = mem;
					break;
				} else if(migrate == null)
				{
					migrate = mem;
				}
			}
			
			// No other valid owners found
			if(migrate == null)
			{
				members.put(uuid, old);
				return;
			} else
			{
				members.put(migrate, EnumPartyStatus.OWNER);
			}
		}
		
		PacketSender.INSTANCE.sendToAll(getSyncPacket());
	}
	
	@Override
	public EnumPartyStatus getStatus(UUID uuid)
	{
		return members.get(uuid);
	}
	
	@Override
	public List<UUID> getMembers()
	{
		return new ArrayList<UUID>(members.keySet());
	}
	
	private void hostMigrate()
	{
		List<UUID> tmp = getMembers();
		
		// Pre check for existing owners
		for(UUID uuid : tmp)
		{
			if(members.get(uuid) == EnumPartyStatus.OWNER)
			{
				return;
			}
		}
		
		UUID migrate = null;
		
		for(UUID mem : getMembers())
		{
			if(members.get(mem) == EnumPartyStatus.ADMIN)
			{
				migrate = mem;
				break;
			} else if(migrate == null)
			{
				migrate = mem;
			}
		}
		
		if(migrate != null)
		{
			members.put(migrate, EnumPartyStatus.OWNER);
		}
	}
	
	@Override
	public PreparedPayload getSyncPacket()
	{
		NBTTagCompound tags = new NBTTagCompound();
		JsonObject base = new JsonObject();
		base.add("party", writeToJson(new JsonObject(), EnumSaveType.CONFIG));
		tags.setTag("data", NBTConverter.JSONtoNBT_Object(base, new NBTTagCompound()));
		tags.setInteger("partyID", PartyManager.INSTANCE.getKey(this));
		
		return new PreparedPayload(PacketTypeNative.PARTY_SYNC.GetLocation(), tags);
	}
	
	@Override
	public void readPacket(NBTTagCompound payload)
	{
		JsonObject base = NBTConverter.NBTtoJSON_Compound(payload.getCompoundTag("data"), new JsonObject());
		
		readFromJson(JsonHelper.GetObject(base, "party"), EnumSaveType.CONFIG);
	}
	
	@Override
	public JsonObject writeToJson(JsonObject json, EnumSaveType saveType)
	{
		json.addProperty("name", name);
		json.addProperty("lifeShare", lifeShare);
		json.addProperty("lootShare", lootShare);
		json.addProperty("lives", lives);
		
		JsonArray memJson = new JsonArray();
		for(Entry<UUID,EnumPartyStatus> mem : members.entrySet())
		{
			JsonObject jm = new JsonObject();
			jm.addProperty("uuid", mem.getKey().toString());
			jm.addProperty("status", mem.getValue().toString());
			memJson.add(jm);
		}
		json.add("members", memJson);
		
		return json;
	}
	
	@Override
	public void readFromJson(JsonObject jObj, EnumSaveType saveType)
	{
		if(saveType != EnumSaveType.CONFIG)
		{
			return;
		}
		
		name = JsonHelper.GetString(jObj, "name", "New Party");
		lifeShare = JsonHelper.GetBoolean(jObj, "lifeShare", false);
		lootShare = JsonHelper.GetBoolean(jObj, "lootShare", false);
		lives = JsonHelper.GetNumber(jObj, "lives", 1).intValue();
		
		members.clear();;
		for(JsonElement entry : JsonHelper.GetArray(jObj, "members"))
		{
			if(entry == null || !entry.isJsonObject())
			{
				continue;
			}
			
			JsonObject jMem = entry.getAsJsonObject();
			UUID uuid = null;
			EnumPartyStatus priv = EnumPartyStatus.INVITE;
			
			try
			{
				uuid = UUID.fromString(JsonHelper.GetString(jMem, "uuid", ""));
			} catch(Exception e)
			{
				uuid = null;
			}
			
			try
			{
				priv = EnumPartyStatus.valueOf(JsonHelper.GetString(jMem, "status", EnumPartyStatus.INVITE.toString()));
				priv = priv != null? priv : EnumPartyStatus.INVITE;
			} catch(Exception e)
			{
				priv = EnumPartyStatus.INVITE;
			}
			
			if(uuid != null)
			{
				members.put(uuid, priv);
			}
		}
	}
}
