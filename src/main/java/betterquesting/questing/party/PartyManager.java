package betterquesting.questing.party;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import betterquesting.api.enums.EnumPartyStatus;
import betterquesting.api.enums.EnumSaveType;
import betterquesting.api.network.QuestingPacket;
import betterquesting.api.questing.party.IParty;
import betterquesting.api.questing.party.IPartyDatabase;
import betterquesting.network.PacketTypeNative;
import betterquesting.storage.NameCache;

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
		tags.setTag("data", writeToNBT(new NBTTagList(), EnumSaveType.CONFIG));
		return new QuestingPacket(PacketTypeNative.PARTY_DATABASE.GetLocation(), tags);
	}
	
	@Override
	public void readPacket(NBTTagCompound payload)
	{
		readFromNBT(payload.getTagList("data", 10), EnumSaveType.CONFIG);
	}
	
	@Override
	public NBTTagList writeToNBT(NBTTagList json, EnumSaveType saveType)
	{
		if(saveType != EnumSaveType.CONFIG)
		{
			return json;
		}
		
		for(Entry<Integer,IParty> entry : partyList.entrySet())
		{
			NBTTagCompound jp = entry.getValue().writeToNBT(new NBTTagCompound(), saveType);
			jp.setInteger("partyID", entry.getKey());
			json.appendTag(jp);
		}
		
		return json;
	}
	
	@Override
	public void readFromNBT(NBTTagList json, EnumSaveType saveType)
	{
		if(saveType != EnumSaveType.CONFIG)
		{
			return;
		}
		
		partyList.clear();
		for(int i = 0; i < json.tagCount(); i++)
		{
			NBTBase element = json.get(i);
			
			if(element == null || element.getId() != 10)
			{
				continue;
			}
			
			NBTTagCompound jp = (NBTTagCompound)element;
			
			int partyID = jp.hasKey("partyID", 99) ? jp.getInteger("partyID") : -1;
			
			if(partyID < 0)
			{
				continue;
			}
			
			IParty party = new PartyInstance();
			party.readFromNBT(jp, EnumSaveType.CONFIG);
			
			if(party.getMembers().size() > 0)
			{
				partyList.put(partyID, party);
			}
		}
	}
}
