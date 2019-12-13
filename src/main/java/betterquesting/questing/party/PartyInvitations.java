package betterquesting.questing.party;

import betterquesting.api.enums.EnumPartyStatus;
import betterquesting.api.questing.party.IParty;
import betterquesting.api2.storage.INBTPartial;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.Map.Entry;

// NOTE: This is in a separate class because it could later be moved to a dedicated inbox system
public class PartyInvitations implements INBTPartial<ListNBT, UUID>
{
    public static final PartyInvitations INSTANCE = new PartyInvitations();
    
	private final HashMap<UUID,HashMap<Integer,Long>> invites = new HashMap<>();
	
    public synchronized  void postInvite(@Nonnull UUID uuid, int id, long expiryTime)
    {
        if(expiryTime <= System.currentTimeMillis()) return; // Can't expire before being issued
        
        IParty party = PartyManager.INSTANCE.getValue(id);
        if(party == null || party.getStatus(uuid) != null) return; // Party doesn't exist or user has already joined
        
        HashMap<Integer,Long> list = invites.computeIfAbsent(uuid, (key) -> new HashMap<>());
        list.put(id, expiryTime);
    }
    
    public synchronized boolean acceptInvite(@Nonnull UUID uuid, int id)
    {
	    HashMap<Integer,Long> userInvites = invites.get(uuid);
	    if(userInvites == null || userInvites.size() <= 0) return false;
	    
	    long timestamp = userInvites.get(id);
	    IParty party = PartyManager.INSTANCE.getValue(id);
	    boolean valid = timestamp > System.currentTimeMillis();
	    
	    if(valid && party != null) party.setStatus(uuid, EnumPartyStatus.MEMBER);
	    
        userInvites.remove(id); // We still remove it regardless of validity
        if(userInvites.size() <= 0) invites.remove(uuid);
	    
        return valid;
    }
	
	public synchronized List<Entry<Integer,Long>> getPartyInvites(@Nonnull UUID uuid)
	{
	    HashMap<Integer,Long> userInvites = invites.get(uuid);
	    if(userInvites == null || userInvites.size() <= 0) return Collections.emptyList();
	    
	    List<Entry<Integer,Long>> list = new ArrayList<>();
        Iterator<Entry<Integer,Long>> iter = userInvites.entrySet().iterator();
        
        while(iter.hasNext())
        {
            Entry<Integer,Long> entry = iter.next();
            if(entry.getValue() <= System.currentTimeMillis())
            {
                iter.remove();
                continue;
            }
            list.add(entry);
        }
        
        // Sort by expiry time
        list.sort(Comparator.comparing(Entry::getValue));
        return list;
	}
	
	// Primarily used when deleting parties to ensure that pending invites don't link to newly created parties under the same ID
	public synchronized void purgeInvites(int partyID)
    {
        invites.values().forEach((value) -> value.remove(partyID));
    }
    
    public synchronized void reset()
    {
        invites.clear();
    }
    
    @Override
    public synchronized ListNBT writeToNBT(ListNBT nbt, @Nullable List<UUID> subset) // Don't bother saving this to disk. We do need to send packets though
    {
        if(subset != null)
        {
            subset.forEach((uuid) -> {
                CompoundNBT userTag = new CompoundNBT();
                userTag.putString("uuid", uuid.toString());
                
                Map<Integer,Long> userMap = invites.get(uuid);
                if(userMap == null) userMap = Collections.emptyMap();
                ListNBT invList = new ListNBT();
                
                for(Entry<Integer, Long> invEntry : userMap.entrySet())
                {
                    CompoundNBT invTag = new CompoundNBT();
                    invTag.putInt("partyID", invEntry.getKey());
                    invTag.putLong("expiry", invEntry.getValue());
                    invList.add(invTag);
                }
                
                userTag.put("invites", invList);
                nbt.add(userTag);
            });
        } else
        {
            for(Entry<UUID, HashMap<Integer, Long>> userMap : invites.entrySet())
            {
                CompoundNBT userTag = new CompoundNBT();
                userTag.putString("uuid", userMap.getKey().toString());
        
                ListNBT invList = new ListNBT();
                for(Entry<Integer, Long> invEntry : userMap.getValue().entrySet())
                {
                    CompoundNBT invTag = new CompoundNBT();
                    invTag.putInt("partyID", invEntry.getKey());
                    invTag.putLong("expiry", invEntry.getValue());
                    invList.add(invTag);
                }
                userTag.put("invites", invList);
                nbt.add(userTag);
            }
        }
        return nbt;
    }
    
    @Override
    public synchronized void readFromNBT(ListNBT nbt, boolean merge)
    {
        if(!merge) invites.clear(); // There's almost no reason to not merge. The expiry times manage themselves on both sides
        for(int i = 0; i < nbt.size(); i++)
        {
            CompoundNBT userEntry = nbt.getCompound(i);
            UUID uuid;
            try
            {
                uuid = UUID.fromString(userEntry.getString("uuid"));
            } catch(Exception e)
            {
                continue;
            }
            
            ListNBT invList = userEntry.getList("invites", 10);
            HashMap<Integer,Long> map = invites.computeIfAbsent(uuid, (key) -> new HashMap<>()); // Could start from scratch but this feels cleaner
            map.clear();
            for(int n = 0; n < invList.size(); n++)
            {
                CompoundNBT invEntry = invList.getCompound(n);
                int partyID = invEntry.contains("partyID", 99) ? invEntry.getInt("partyID") : -1;
                long timestamp = invEntry.contains("expiry", 99) ? invEntry.getLong("expiry") : -1;
                if(partyID < 0 || timestamp < System.currentTimeMillis()) continue;
                map.put(partyID, timestamp);
            }
        }
    }
}
