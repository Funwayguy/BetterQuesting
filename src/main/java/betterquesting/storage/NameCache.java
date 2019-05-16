package betterquesting.storage;

import betterquesting.api.network.QuestingPacket;
import betterquesting.api.storage.INameCache;
import betterquesting.network.PacketSender;
import betterquesting.network.PacketTypeNative;
import com.mojang.authlib.GameProfile;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.server.MinecraftServer;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class NameCache implements INameCache
{
	public static final NameCache INSTANCE = new NameCache();
	
	// TODO: Proper thread safety
    // TODO: Label known names as offline/online and convert accordingly
	private final ConcurrentHashMap<UUID,NBTTagCompound> cache = new ConcurrentHashMap<>();
	
	@Override
    public void setName(UUID uuid, String name)
    {
        if(uuid == null || name == null) return;
        
        synchronized(cache)
        {
            NBTTagCompound tag = cache.get(uuid);
            
            if(tag == null)
            {
                tag = new NBTTagCompound();
                tag.setBoolean("isOP", false);
            }
            
            tag.setString("name", name);
        }
    }
	
	@Override
	public String getName(UUID uuid)
	{
	    if(uuid == null) return null;
	    
	    synchronized(cache)
        {
            if(!cache.containsKey(uuid))
            {
                return uuid.toString();
            } else
            {
                return cache.get(uuid).getString("name");
            }
        }
	}
	
	@Override
	public UUID getUUID(String name)
	{
	    if(name == null) return null;
	    
	    synchronized(cache)
        {
            for(Entry<UUID, NBTTagCompound> entry : cache.entrySet())
            {
                if(entry.getValue().getString("name").equalsIgnoreCase(name))
                {
                    return entry.getKey();
                }
            }
        }
		
		return null;
	}
	
	@Override
	public boolean isOP(UUID uuid)
	{
	    if(uuid == null) return false;
	    
	    synchronized(cache)
        {
            if(!cache.containsKey(uuid))
            {
                return false;
            } else
            {
                return cache.get(uuid).getBoolean("isOP");
            }
        }
	}
	
	@Override
	public void updateNames(MinecraftServer server)
	{
		String[] names = server.getPlayerProfileCache().getUsernames();
		
		for(String name : names)
		{
			EntityPlayerMP player = server.getPlayerList().getPlayerByUsername(name);
			GameProfile prof = player == null? null : player.getGameProfile();
			
			if(prof != null)
			{
			    synchronized(cache)
                {
                    UUID oldID = getUUID(prof.getName());
    
                    while(oldID != null)
                    {
                        // Cleans out all name duplicates
                        cache.remove(oldID);
                        oldID = getUUID(prof.getName());
                    }
    
                    NBTTagCompound json = new NBTTagCompound();
                    json.setString("name", prof.getName());
                    json.setBoolean("isOP", server.getPlayerList().canSendCommands(prof));
                    cache.put(prof.getId(), json);
                }
			}
		}
		
		PacketSender.INSTANCE.sendToAll(getSyncPacket(null));
	}
	
	@Override
	public int size()
	{
	    synchronized(cache)
        {
            return cache.size();
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
		tags.setTag("data", this.writeToNBT(new NBTTagList(), users));
		return new QuestingPacket(PacketTypeNative.NAME_CACHE.GetLocation(), tags);
	}
	
	@Override
	public void readPacket(NBTTagCompound payload)
	{
		readFromNBT(payload.getTagList("data", 10), false);
	}

	@Override
	public NBTTagList writeToNBT(NBTTagList json, List<UUID> users)
	{
		synchronized(cache)
        {
            for(Entry<UUID, NBTTagCompound> entry : cache.entrySet())
            {
                NBTTagCompound jn = new NBTTagCompound();
                jn.setString("uuid", entry.getKey().toString());
                jn.setString("name", entry.getValue().getString("name"));
                jn.setBoolean("isOP", entry.getValue().getBoolean("isOP"));
                json.appendTag(jn);
            }
        }
		
		return json;
	}

	@Override
	public void readFromNBT(NBTTagList nbt, boolean merge)
	{
		synchronized(cache)
        {
            cache.clear();
            for(int i = 0; i < nbt.tagCount(); i++)
            {
                NBTBase element = nbt.get(i);
        
                if(element.getId() != 10)
                {
                    continue;
                }
        
                NBTTagCompound jn = (NBTTagCompound)element;
        
                try
                {
                    UUID uuid = UUID.fromString(jn.getString("uuid"));
                    String name = jn.getString("name");
                    boolean isOP = jn.getBoolean("isOP");
            
                    NBTTagCompound j2 = new NBTTagCompound();
                    j2.setString("name", name);
                    j2.setBoolean("isOP", isOP);
                    cache.put(uuid, j2);
                } catch(Exception ignored){}
            }
        }
	}
	
	@Override
	public void reset()
	{
	    synchronized(cache)
        {
            cache.clear();
        }
	}
	
	@Override
	public List<String> getAllNames()
	{
		List<String> list = new ArrayList<>();
		
		synchronized(cache)
        {
            for(NBTTagCompound json : cache.values())
            {
                if(json != null && json.hasKey("name", 8))
                {
                    list.add(json.getString("name"));
                }
            }
        }
		
		return list;
	}
}
