package betterquesting.questing.party;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import betterquesting.api.enums.EnumPartyStatus;
import betterquesting.api.enums.EnumSaveType;
import betterquesting.api.network.QuestingPacket;
import betterquesting.api.properties.IPropertyContainer;
import betterquesting.api.properties.IPropertyType;
import betterquesting.api.properties.NativeProps;
import betterquesting.api.questing.party.IParty;
import betterquesting.network.PacketSender;
import betterquesting.network.PacketTypeNative;
import betterquesting.storage.PropertyContainer;

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
		tags.setTag("data", writeToNBT(new NBTTagCompound(), EnumSaveType.CONFIG));
		tags.setInteger("partyID", PartyManager.INSTANCE.getKey(this));
		
		return new QuestingPacket(PacketTypeNative.PARTY_SYNC.GetLocation(), tags);
	}
	
	@Override
	public void readPacket(NBTTagCompound payload)
	{
		readFromNBT(payload.getCompoundTag("data"), EnumSaveType.CONFIG);
	}
	
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound json, EnumSaveType saveType)
	{
		if(saveType != EnumSaveType.CONFIG)
		{
			return json;
		}
		
		NBTTagList memJson = new NBTTagList();
		for(Entry<UUID,EnumPartyStatus> mem : members.entrySet())
		{
			NBTTagCompound jm = new NBTTagCompound();
			jm.setString("uuid", mem.getKey().toString());
			jm.setString("status", mem.getValue().toString());
			memJson.appendTag(jm);
		}
		json.setTag("members", memJson);
		
		json.setTag("properties", pInfo.writeToNBT(new NBTTagCompound(), EnumSaveType.CONFIG));
		
		return json;
	}
	
	@Override
	public void readFromNBT(NBTTagCompound jObj, EnumSaveType saveType)
	{
		if(saveType != EnumSaveType.CONFIG)
		{
			return;
		}
		
		if(jObj.hasKey("properties", 10))
		{
			pInfo.readFromNBT(jObj.getCompoundTag("properties"), EnumSaveType.CONFIG);
		} else // Legacy stuff
		{
			pInfo.readFromNBT(new NBTTagCompound(), EnumSaveType.CONFIG);
			pInfo.setProperty(NativeProps.NAME, jObj.getString("name"));
			pInfo.setProperty(NativeProps.PARTY_LIVES, jObj.getBoolean("lifeShare"));
			pInfo.setProperty(NativeProps.PARTY_LOOT, jObj.getBoolean("lootShare"));
			pInfo.setProperty(NativeProps.LIVES, jObj.getInteger("lives"));
		}
		
		members.clear();
		NBTTagList memList = jObj.getTagList("members", 10);
		for(int i = 0; i < memList.tagCount(); i++)
		{
			NBTBase entry = memList.get(i);
			
			if(entry == null || entry.getId() != 10)
			{
				continue;
			}
			
			NBTTagCompound jMem = (NBTTagCompound)entry;
			UUID uuid = null;
			EnumPartyStatus priv = EnumPartyStatus.INVITE;
			
			try
			{
				uuid = UUID.fromString(jMem.getString("uuid"));
			} catch(Exception e)
			{
				uuid = null;
			}
			
			try
			{
				priv = EnumPartyStatus.valueOf(jMem.hasKey("status", 8) ? jMem.getString("status") : EnumPartyStatus.INVITE.toString());
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
}
