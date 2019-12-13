package betterquesting.storage;

import betterquesting.api.storage.INameCache;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.Map.Entry;

public final class NameCache implements INameCache
{
	public static final NameCache INSTANCE = new NameCache();
	
    // TODO: Label known names as offline/online and convert accordingly?
	private final HashMap<UUID, CompoundNBT> cache = new HashMap<>();
	
	@Override
    public synchronized boolean updateName(@Nonnull ServerPlayerEntity player)
    {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        CompoundNBT tag = cache.computeIfAbsent(player.getGameProfile().getId(), (key) -> new CompoundNBT());
        
        String name = player.getGameProfile().getName();
        boolean isOP = server.getPlayerList().canSendCommands(player.getGameProfile());
        
        if(!tag.getString("name").equals(name) || tag.getBoolean("isOP") != isOP)
        {
            tag.putString("name", name);
            tag.putBoolean("isOP", isOP);
            return true;
        }
        
        return false;
    }
	
	@Override
	public synchronized String getName(@Nonnull UUID uuid)
	{
	    CompoundNBT tag = cache.get(uuid);
	    return tag == null ? uuid.toString() : tag.getString("name");
	}
	
	@Override
	public synchronized UUID getUUID(@Nonnull String name)
	{
        for(Entry<UUID, CompoundNBT> entry : cache.entrySet())
        {
            if(entry.getValue().getString("name").equalsIgnoreCase(name))
            {
                return entry.getKey();
            }
        }
		
		return null;
	}
	
	@Override
	public synchronized boolean isOP(@Nonnull UUID uuid)
	{
	    CompoundNBT tag = cache.get(uuid);
	    return tag != null && tag.getBoolean("isOP");
	}
	
	@Override
	public synchronized int size()
	{
        return cache.size();
	}

	@Override
	public synchronized ListNBT writeToNBT(ListNBT nbt, @Nullable List<UUID> users)
	{
        for(Entry<UUID, CompoundNBT> entry : cache.entrySet())
        {
            if(users != null && !users.contains(entry.getKey())) continue;
            CompoundNBT jn = new CompoundNBT();
            jn.putString("uuid", entry.getKey().toString());
            jn.putString("name", entry.getValue().getString("name"));
            jn.putBoolean("isOP", entry.getValue().getBoolean("isOP"));
            nbt.add(jn);
        }
		
		return nbt;
	}

	@Override
	public synchronized void readFromNBT(ListNBT nbt, boolean merge)
	{
        if(!merge) cache.clear();
        for(int i = 0; i < nbt.size(); i++)
        {
            CompoundNBT jn = nbt.getCompound(i);
    
            try
            {
                UUID uuid = UUID.fromString(jn.getString("uuid"));
                String name = jn.getString("name");
                boolean isOP = jn.getBoolean("isOP");
        
                CompoundNBT j2 = new CompoundNBT();
                j2.putString("name", name);
                j2.putBoolean("isOP", isOP);
                cache.put(uuid, j2);
            } catch(Exception ignored){}
        }
	}
	
	@Override
	public synchronized void reset()
	{
        cache.clear();
        nameCache = null;
	}
	
	private List<String> nameCache = null;
	
	@Override
	public synchronized List<String> getAllNames()
	{
	    if(nameCache != null) return nameCache;
	    
		nameCache = new ArrayList<>();
		
        for(CompoundNBT tag : cache.values())
        {
            if(tag != null && tag.contains("name", 8))
            {
                nameCache.add(tag.getString("name"));
            }
        }
		
		return Collections.unmodifiableList(nameCache);
	}
}
