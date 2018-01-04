package betterquesting.storage;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.server.MinecraftServer;
import betterquesting.api.enums.EnumSaveType;
import betterquesting.api.network.QuestingPacket;
import betterquesting.api.storage.INameCache;
import betterquesting.network.PacketSender;
import betterquesting.network.PacketTypeNative;
import com.mojang.authlib.GameProfile;

public final class NameCache implements INameCache
{
	public static final NameCache INSTANCE = new NameCache();
	
	private final ConcurrentHashMap<UUID,NBTTagCompound> cache = new ConcurrentHashMap<UUID,NBTTagCompound>();
	
	private NameCache()
	{
	}
	
	@Override
	public String getName(UUID uuid)
	{
		if(!cache.containsKey(uuid))
		{
			return uuid.toString();
		} else
		{
			return cache.get(uuid).getString("name");
		}
	}
	
	@Override
	public UUID getUUID(String name)
	{
		for(Entry<UUID,NBTTagCompound> entry : cache.entrySet())
		{
			if(entry.getValue().getString("name").equalsIgnoreCase(name))
			{
				return entry.getKey();
			}
		}
		
		return null;
	}
	
	@Override
	public boolean isOP(UUID uuid)
	{
		if(!cache.containsKey(uuid))
		{
			return false;
		} else
		{
			return cache.get(uuid).getBoolean("isOP");
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
		
		PacketSender.INSTANCE.sendToAll(getSyncPacket());
	}
	
	@Override
	public int size()
	{
		return cache.size();
	}
	
	@Override
	public QuestingPacket getSyncPacket()
	{
		NBTTagCompound tags = new NBTTagCompound();
		tags.setTag("data", this.writeToNBT(new NBTTagList(), EnumSaveType.CONFIG));
		return new QuestingPacket(PacketTypeNative.NAME_CACHE.GetLocation(), tags);
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
		
		for(Entry<UUID,NBTTagCompound> entry : cache.entrySet())
		{
			NBTTagCompound jn = new NBTTagCompound();
			jn.setString("uuid", entry.getKey().toString());
			jn.setString("name", entry.getValue().getString("name"));
			jn.setBoolean("isOP", entry.getValue().getBoolean("isOP"));
			json.appendTag(jn);
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
		
		cache.clear();
		for(int i = 0; i < json.tagCount(); i++)
		{
			NBTBase element = json.get(i);
			if(element == null || element.getId() != 10)
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
			} catch(Exception e)
			{
				continue;
			}
		}
	}

	public void reset()
	{
		cache.clear();
	}
	
	@Override
	public List<String> getAllNames()
	{
		List<String> list = new ArrayList<String>();
		
		for(NBTTagCompound json : cache.values())
		{
			if(json != null && json.hasKey("name", 8))
			{
				list.add(json.getString("name"));
			}
		}
		
		return list;
	}
}
