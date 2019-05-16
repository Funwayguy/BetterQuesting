package betterquesting.questing.party;

import betterquesting.api.enums.EnumPartyStatus;
import betterquesting.api.network.QuestingPacket;
import betterquesting.api.properties.NativeProps;
import betterquesting.api.questing.party.IParty;
import betterquesting.api.questing.party.IPartyDatabase;
import betterquesting.api2.storage.DBEntry;
import betterquesting.api2.storage.SimpleDatabase;
import betterquesting.network.PacketTypeNative;
import betterquesting.storage.NameCache;
import betterquesting.storage.QuestSettings;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class PartyManager extends SimpleDatabase<IParty> implements IPartyDatabase
{
	public static final PartyManager INSTANCE = new PartyManager();
	
	private final HashMap<UUID,Integer> partyCache = new HashMap<>();
	
	@Override
	public IParty getUserParty(UUID uuid)
	{
	    if(!QuestSettings.INSTANCE.getProperty(NativeProps.PARTY_ENABLE)) return null;
	    
	    synchronized(partyCache)
        {
            Integer cachedID = partyCache.get(uuid);
            IParty cachedParty = cachedID == null ? null : getValue(cachedID);
    
            if(cachedID != null && cachedParty == null) // Disbanded party
            {
                partyCache.remove(uuid);
            } else if(cachedParty != null) // Active party. Check validity...
            {
                EnumPartyStatus status = cachedParty.getStatus(uuid);
                if(status != null && status != EnumPartyStatus.INVITE) return cachedParty;
                partyCache.remove(uuid); // User isn't a party member anymore
            }
        }
	    
	    // NOTE: A server with a lot of solo players may still hammer this loop. Optimise further?
		for(DBEntry<IParty> entry : getEntries())
		{
			EnumPartyStatus status = entry.getValue().getStatus(uuid);
			
			if(status != null && status != EnumPartyStatus.INVITE)
			{
			    partyCache.put(uuid, entry.getID());
				return entry.getValue();
			}
		}
		
		return null;
	}
	
	@Override
	public List<Integer> getPartyInvites(UUID uuid)
	{
		List<Integer> invites = new ArrayList<>();
		
		boolean isOp = NameCache.INSTANCE.isOP(uuid);
		
		for(DBEntry<IParty> entry : getEntries())
		{
			if(isOp || entry.getValue().getStatus(uuid) == EnumPartyStatus.INVITE)
			{
				invites.add(entry.getID());
			}
		}
		
		return invites;
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
		tags.setTag("data", writeToNBT(new NBTTagList(), users));
		return new QuestingPacket(PacketTypeNative.PARTY_DATABASE.GetLocation(), tags);
	}
	
	@Override
    @Deprecated
	public void readPacket(NBTTagCompound payload)
	{
		readFromNBT(payload.getTagList("data", 10), false);
	}
	
	@Override
	public NBTTagList writeToNBT(NBTTagList json, List<UUID> users)
	{
		for(DBEntry<IParty> entry : getEntries())
		{
			NBTTagCompound jp = entry.getValue().writeToNBT(new NBTTagCompound());
			jp.setInteger("partyID", entry.getID());
			json.appendTag(jp);
		}
		
		return json;
	}
	
	@Override
	public void readFromNBT(NBTTagList json, boolean merge)
	{
		reset();
		
		for(int i = 0; i < json.tagCount(); i++)
		{
			NBTBase element = json.get(i);
			
			if(element.getId() != 10)
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
			party.readFromNBT(jp);
			
			if(party.getMembers().size() > 0)
			{
				add(partyID, party);
			}
		}
	}
	
	@Override
    public void reset()
    {
        super.reset();
        
        synchronized(partyCache)
        {
            partyCache.clear();
        }
    }
}
