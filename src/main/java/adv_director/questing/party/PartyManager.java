package adv_director.questing.party;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.nbt.NBTTagCompound;
import adv_director.api.enums.EnumPartyStatus;
import adv_director.api.enums.EnumSaveType;
import adv_director.api.network.QuestingPacket;
import adv_director.api.questing.party.IParty;
import adv_director.api.questing.party.IPartyDatabase;
import adv_director.api.utils.JsonHelper;
import adv_director.api.utils.NBTConverter;
import adv_director.network.PacketTypeNative;
import adv_director.storage.NameCache;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public final class PartyManager implements IPartyDatabase
{
	public static final PartyManager INSTANCE = new PartyManager();
	
	private final ConcurrentHashMap<Integer, IParty> partyList = new ConcurrentHashMap<Integer, IParty>();
	
	private PartyManager()
	{
	}
	
	@Override
	public IParty getUserParty(UUID uuid)
	{
		for(IParty p : getAllValues())
		{
			EnumPartyStatus status = p.getStatus(uuid);
			
			if(status != null && status != EnumPartyStatus.INVITE)
			{
				return p;
			}
		}
		
		return null;
	}
	
	@Override
	public List<Integer> getPartyInvites(UUID uuid)
	{
		ArrayList<Integer> invites = new ArrayList<Integer>();
		
		boolean isOp = NameCache.INSTANCE.isOP(uuid);
		
		for(Entry<Integer,IParty> entry : partyList.entrySet())
		{
			if(isOp || entry.getValue().getStatus(uuid) == EnumPartyStatus.INVITE)
			{
				invites.add(entry.getKey());
			}
		}
		
		return invites;
	}
	
	@Override
	public Integer nextKey()
	{
		int i = 0;
		
		while(partyList.containsKey(i))
		{
			i++;
		}
		
		return i;
	}
	
	@Override
	public boolean add(IParty party, Integer id)
	{
		if(party == null || id < 0 || partyList.containsKey(id) || partyList.containsValue(party))
		{
			return false;
		}
		
		partyList.put(id, party);
		return true;
	}
	
	@Override
	public boolean removeKey(Integer id)
	{
		return partyList.remove(id) != null;
	}
	
	@Override
	public boolean removeValue(IParty party)
	{
		return removeKey(getKey(party));
	}
	
	@Override
	public IParty getValue(Integer id)
	{
		return partyList.get(id);
	}
	
	@Override
	public Integer getKey(IParty party)
	{
		for(Entry<Integer,IParty> entry : partyList.entrySet())
		{
			if(entry.getValue() == party)
			{
				return entry.getKey();
			}
		}
		
		return -1;
	}
	
	@Override
	public int size()
	{
		return partyList.size();
	}
	
	@Override
	public void reset()
	{
		partyList.clear();
	}
	
	@Override
	public List<IParty> getAllValues()
	{
		return new ArrayList<IParty>(partyList.values());
	}
	
	@Override
	public List<Integer> getAllKeys()
	{
		return new ArrayList<Integer>(partyList.keySet());
	}
	
	@Override
	public QuestingPacket getSyncPacket()
	{
		NBTTagCompound tags = new NBTTagCompound();
		JsonObject json = new JsonObject();
		json.add("parties", writeToJson(new JsonArray(), EnumSaveType.CONFIG));
		tags.setTag("data", NBTConverter.JSONtoNBT_Object(json, new NBTTagCompound()));
		return new QuestingPacket(PacketTypeNative.PARTY_DATABASE.GetLocation(), tags);
	}
	
	@Override
	public void readPacket(NBTTagCompound payload)
	{
		JsonObject json = NBTConverter.NBTtoJSON_Compound(payload.getCompoundTag("data"), new JsonObject());
		
		readFromJson(JsonHelper.GetArray(json, "parties"), EnumSaveType.CONFIG);
	}
	
	@Override
	public JsonArray writeToJson(JsonArray json, EnumSaveType saveType)
	{
		if(saveType != EnumSaveType.CONFIG)
		{
			return json;
		}
		
		for(Entry<Integer,IParty> entry : partyList.entrySet())
		{
			JsonObject jp = entry.getValue().writeToJson(new JsonObject(), saveType);
			jp.addProperty("partyID", entry.getKey());
			json.add(jp);
		}
		
		return json;
	}
	
	@Override
	public void readFromJson(JsonArray json, EnumSaveType saveType)
	{
		if(saveType != EnumSaveType.CONFIG)
		{
			return;
		}
		
		partyList.clear();
		for(JsonElement element : json)
		{
			if(element == null || !element.isJsonObject())
			{
				continue;
			}
			
			JsonObject jp = element.getAsJsonObject();
			
			int partyID = JsonHelper.GetNumber(jp, "partyID", -1).intValue();
			
			if(partyID < 0)
			{
				continue;
			}
			
			IParty party = new PartyInstance();
			party.readFromJson(jp, EnumSaveType.CONFIG);
			
			if(party.getMembers().size() > 0)
			{
				partyList.put(partyID, party);
			}
		}
	}
}
