package adv_director.questing.party;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;
import net.minecraft.nbt.NBTTagCompound;
import adv_director.api.enums.EnumPartyStatus;
import adv_director.api.enums.EnumSaveType;
import adv_director.api.network.QuestingPacket;
import adv_director.api.properties.IPropertyContainer;
import adv_director.api.properties.IPropertyType;
import adv_director.api.properties.NativeProps;
import adv_director.api.questing.party.IParty;
import adv_director.api.utils.JsonHelper;
import adv_director.api.utils.NBTConverter;
import adv_director.network.PacketSender;
import adv_director.network.PacketTypeNative;
import adv_director.storage.PropertyContainer;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class PartyInstance implements IParty
{
	private HashMap<UUID, EnumPartyStatus> members = new HashMap<UUID, EnumPartyStatus>();
	private PropertyContainer pInfo = new PropertyContainer();
	
	public PartyInstance()
	{
		this.setupProps();
	}
	
	private void setupProps()
	{
		setupValue(NativeProps.NAME, "New Party");
		setupValue(NativeProps.PARTY_LIVES);
		setupValue(NativeProps.PARTY_LOOT);
	}
	
	private <T> void setupValue(IPropertyType<T> prop)
	{
		this.setupValue(prop, prop.getDefault());
	}
	
	private <T> void setupValue(IPropertyType<T> prop, T def)
	{
		pInfo.setProperty(prop, pInfo.getProperty(prop, def));
	}
	
	@Override
	public String getName()
	{
		return pInfo.getProperty(NativeProps.NAME, "New Party");
	}
	
	@Override
	@Deprecated
	public boolean getShareLives()
	{
		return pInfo.getProperty(NativeProps.PARTY_LIVES, false);
	}
	
	@Override
	@Deprecated
	public boolean getShareReward()
	{
		return false;
	}
	
	@Override
	public IPropertyContainer getProperties()
	{
		return pInfo;
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
	public QuestingPacket getSyncPacket()
	{
		NBTTagCompound tags = new NBTTagCompound();
		JsonObject base = new JsonObject();
		base.add("party", writeToJson(new JsonObject(), EnumSaveType.CONFIG));
		tags.setTag("data", NBTConverter.JSONtoNBT_Object(base, new NBTTagCompound()));
		tags.setInteger("partyID", PartyManager.INSTANCE.getKey(this));
		
		return new QuestingPacket(PacketTypeNative.PARTY_SYNC.GetLocation(), tags);
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
		if(saveType != EnumSaveType.CONFIG)
		{
			return json;
		}
		
		JsonArray memJson = new JsonArray();
		for(Entry<UUID,EnumPartyStatus> mem : members.entrySet())
		{
			JsonObject jm = new JsonObject();
			jm.addProperty("uuid", mem.getKey().toString());
			jm.addProperty("status", mem.getValue().toString());
			memJson.add(jm);
		}
		json.add("members", memJson);
		
		json.add("properties", pInfo.writeToJson(new JsonObject(), EnumSaveType.CONFIG));
		
		return json;
	}
	
	@Override
	public void readFromJson(JsonObject jObj, EnumSaveType saveType)
	{
		if(saveType != EnumSaveType.CONFIG)
		{
			return;
		}
		
		if(jObj.has("properties"))
		{
			pInfo.readFromJson(JsonHelper.GetObject(jObj, "properties"), EnumSaveType.CONFIG);
		} else
		{
			pInfo.readFromJson(new JsonObject(), EnumSaveType.CONFIG);
			pInfo.setProperty(NativeProps.NAME, JsonHelper.GetString(jObj, "name", "New Party"));
			pInfo.setProperty(NativeProps.PARTY_LIVES, JsonHelper.GetBoolean(jObj, "lifeShare", false));
			pInfo.setProperty(NativeProps.PARTY_LOOT, JsonHelper.GetBoolean(jObj, "lootShare", false));
			pInfo.setProperty(NativeProps.LIVES, JsonHelper.GetNumber(jObj, "lives", 1).intValue());
		}
		
		members.clear();
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
		
		this.setupProps();
	}
	
	@Override
	@Deprecated
	public void setName(String name)
	{
		pInfo.setProperty(NativeProps.NAME, name);
	}
	
	@Override
	@Deprecated
	public void setShareLives(boolean state)
	{
		pInfo.setProperty(NativeProps.PARTY_LIVES, state);
	}
	
	@Override
	@Deprecated
	public void setShareReward(boolean state)
	{
		pInfo.setProperty(NativeProps.PARTY_LOOT, state);
	}
}
