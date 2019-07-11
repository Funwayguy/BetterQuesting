package betterquesting.storage;

import betterquesting.api.storage.INameCache;
import com.mojang.authlib.GameProfile;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.server.MinecraftServer;

import javax.annotation.Nullable;
import java.util.*;
import java.util.Map.Entry;

public final class NameCache implements INameCache
{
	public static final NameCache INSTANCE = new NameCache();
	
    // TODO: Label known names as offline/online and convert accordingly?
	private final HashMap<UUID,NBTTagCompound> cache = new HashMap<>();
	
	@Override
    public synchronized void setName(UUID uuid, String name)
    {
        if(uuid == null || name == null) return;
        
        NBTTagCompound tag = cache.get(uuid);
        
        if(tag == null)
        {
            tag = new NBTTagCompound();
            tag.setBoolean("isOP", false);
        }
        
        tag.setString("name", name);
    }
	
	@Override
	public synchronized String getName(UUID uuid)
	{
	    if(uuid == null) return null;
	    
        if(!cache.containsKey(uuid))
        {
            return uuid.toString();
        } else
        {
            return cache.get(uuid).getString("name");
        }
	}
	
	@Override
	public synchronized UUID getUUID(String name)
	{
	    if(name == null) return null;
	    
        for(Entry<UUID, NBTTagCompound> entry : cache.entrySet())
        {
            if(entry.getValue().getString("name").equalsIgnoreCase(name))
            {
                return entry.getKey();
            }
        }
		
		return null;
	}
	
	@Override
	public synchronized boolean isOP(UUID uuid)
	{
	    if(uuid == null) return false;
	    
        if(!cache.containsKey(uuid))
        {
            return false;
        } else
        {
            return cache.get(uuid).getBoolean("isOP");
        }
	}
	
	@Override
	public synchronized void updateNames(MinecraftServer server)
	{
	    nameCache = null;
		String[] names = server.getPlayerProfileCache().getUsernames();
		
		for(String name : names)
		{
			EntityPlayerMP player = server.getPlayerList().getPlayerByUsername(name);
			GameProfile prof = player == null? null : player.getGameProfile();
			
			if(prof != null)
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
	
	@Override
	public synchronized int size()
	{
        return cache.size();
	}

	@Override
	public synchronized NBTTagList writeToNBT(NBTTagList json, @Nullable List<UUID> users)
	{
        for(Entry<UUID, NBTTagCompound> entry : cache.entrySet())
        {
            if(users != null && !users.contains(entry.getKey())) continue;
            NBTTagCompound jn = new NBTTagCompound();
            jn.setString("uuid", entry.getKey().toString());
            jn.setString("name", entry.getValue().getString("name"));
            jn.setBoolean("isOP", entry.getValue().getBoolean("isOP"));
            json.appendTag(jn);
        }
		
		return json;
	}

	@Override
	public synchronized void readFromNBT(NBTTagList nbt, boolean merge)
	{
        if(!merge) cache.clear();
        for(int i = 0; i < nbt.tagCount(); i++)
        {
            NBTTagCompound jn = nbt.getCompoundTagAt(i);
    
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
	
	@Override
	public synchronized void reset()
	{
        cache.clear();
	}
	
	private List<String> nameCache = null;
	
	@Override
	public synchronized List<String> getAllNames()
	{
	    if(nameCache != null) return nameCache;
	    
		nameCache = new ArrayList<>();
		
        for(NBTTagCompound tag : cache.values())
        {
            if(tag != null && tag.hasKey("name", 8))
            {
                nameCache.add(tag.getString("name"));
            }
        }
		
		return Collections.unmodifiableList(nameCache);
	}
}
