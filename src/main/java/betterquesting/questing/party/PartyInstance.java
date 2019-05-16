package betterquesting.questing.party;

import betterquesting.api.enums.EnumPartyStatus;
import betterquesting.api.network.QuestingPacket;
import betterquesting.api.properties.IPropertyContainer;
import betterquesting.api.properties.IPropertyType;
import betterquesting.api.properties.NativeProps;
import betterquesting.api.questing.party.IParty;
import betterquesting.core.BetterQuesting;
import betterquesting.network.PacketSender;
import betterquesting.network.PacketTypeNative;
import betterquesting.storage.PropertyContainer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;

public class PartyInstance implements IParty
{
	private final HashMap<UUID, EnumPartyStatus> members = new HashMap<>();
	private final PropertyContainer pInfo = new PropertyContainer();
	
	private final List<UUID> memCache = new ArrayList<>();
	
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
	
	private void refreshCache()
    {
        memCache.clear();
        
        for(Entry<UUID, EnumPartyStatus> entry : members.entrySet())
        {
            if(entry.getValue() != EnumPartyStatus.INVITE)
            {
                memCache.add(entry.getKey());
            }
        }
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
		
		refreshCache();
		PacketSender.INSTANCE.sendToAll(getSyncPacket(null));
	}
	
	@Override
	public void kickUser(UUID uuid)
	{
		if(!members.containsKey(uuid)) return;
		
		EnumPartyStatus old = members.get(uuid);
		List<UUID> notifyMems = new ArrayList<>(members.keySet());
		
		if(members.remove(uuid) == null)
        {
            BetterQuesting.logger.error("Unabled to locate user \"" + uuid + "\" to kick from party " + this.getName());
            return;
        }
        
		if(members.size() <= 0)
		{
			PartyManager.INSTANCE.removeValue(this);
			PacketSender.INSTANCE.sendToAll(PartyManager.INSTANCE.getSyncPacket(notifyMems));
		} else if(old == EnumPartyStatus.OWNER)
		{
			hostMigrate();
            refreshCache();
		    PacketSender.INSTANCE.sendToAll(getSyncPacket(null));
		}
	}
	
	@Override
	public void setStatus(UUID uuid, EnumPartyStatus priv)
	{
		if(!members.containsKey(uuid))
		{
			return;
		}
		
		EnumPartyStatus old = members.get(uuid);
		List<UUID> notifyMems = new ArrayList<>(members.keySet());
		
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
				if(mem == uuid) continue;
				
				if(members.get(mem) == EnumPartyStatus.ADMIN)
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
		
		refreshCache();
		PacketSender.INSTANCE.sendToAll(getSyncPacket(null));
	}
	
	@Override
	public EnumPartyStatus getStatus(UUID uuid)
	{
		return members.get(uuid);
	}
	
	@Override
	public List<UUID> getMembers()
	{
		return memCache;
	}
	
	private void hostMigrate()
	{
	    System.out.println("Migrating host...");
		// Pre check for existing owners
		for(UUID uuid : members.keySet())
		{
			if(members.get(uuid) == EnumPartyStatus.OWNER)
			{
				return;
			}
		}
		
		UUID migrate = null;
		
		for(UUID mem : members.keySet())
		{
		    EnumPartyStatus status = members.get(mem);
		    
			if(status == EnumPartyStatus.ADMIN || status == EnumPartyStatus.OWNER)
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
		} else
        {
            BetterQuesting.logger.error("Failed to find suitable host to migrate party " + this.getName() + ". This should not happen and may now requires an admin to disband this party.");
        }
	}
	
	@Override
    @Deprecated
	public QuestingPacket getSyncPacket()
	{
		return getSyncPacket(null);
	}
	
	@Override
	public QuestingPacket getSyncPacket(@Nullable List<UUID> users)
	{
		NBTTagCompound tags = new NBTTagCompound();
		tags.setTag("data", writeToNBT(new NBTTagCompound()));
		tags.setInteger("partyID", PartyManager.INSTANCE.getID(this));
		
		return new QuestingPacket(PacketTypeNative.PARTY_SYNC.GetLocation(), tags);
	}
	
	@Override
    @Deprecated
	public void readPacket(NBTTagCompound payload)
	{
		readFromNBT(payload.getCompoundTag("data"));
	}
	
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound json)
	{
		NBTTagList memJson = new NBTTagList();
		for(Entry<UUID,EnumPartyStatus> mem : members.entrySet())
		{
			NBTTagCompound jm = new NBTTagCompound();
			jm.setString("uuid", mem.getKey().toString());
			jm.setString("status", mem.getValue().toString());
			memJson.appendTag(jm);
		}
		json.setTag("members", memJson);
		
		json.setTag("properties", pInfo.writeToNBT(new NBTTagCompound()));
		
		return json;
	}
	
	@Override
	public void readFromNBT(NBTTagCompound jObj)
	{
		if(jObj.hasKey("properties", 10))
		{
			pInfo.readFromNBT(jObj.getCompoundTag("properties"));
		} else // Legacy stuff
		{
			pInfo.readFromNBT(new NBTTagCompound());
			pInfo.setProperty(NativeProps.NAME, jObj.getString("name"));
			pInfo.setProperty(NativeProps.PARTY_LIVES, jObj.getBoolean("lifeShare"));
			pInfo.setProperty(NativeProps.PARTY_LOOT, jObj.getBoolean("lootShare"));
			pInfo.setProperty(NativeProps.LIVES, jObj.getInteger("lives"));
		}
		
		members.clear();
		NBTTagList memList = jObj.getTagList("members", 10);
		for(int i = 0; i < memList.tagCount(); i++)
		{
			NBTTagCompound jMem = memList.getCompoundTagAt(i);
			UUID uuid;
			EnumPartyStatus priv;
			
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
			} catch(Exception e)
			{
				priv = EnumPartyStatus.INVITE;
			}
			
			if(uuid != null)
			{
				members.put(uuid, priv);
			}
		}
		
		refreshCache();
		this.setupProps();
	}
}
