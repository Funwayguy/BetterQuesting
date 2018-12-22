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
	
	/**
	 * Updates the cache when a player joins the world.
	 */
	public void updateName(MinecraftServer server, EntityPlayerMP player)
	{
		GameProfile prof = player == null? null : player.getGameProfile();
		if(prof == null) return;

		removeUserFromCache(prof.getName());
		
		boolean isOp = server.getPlayerList().canSendCommands(prof);
		NBTTagCompound json = new NBTTagCompound();
		json.setString("name", prof.getName());
		json.setBoolean("isOP", isOp);
		cache.put(prof.getId(), json);

		PacketSender.INSTANCE.sendToAll(getUpdateSyncPacket(prof.getId(), prof.getName(), isOp));
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
				removeUserFromCache(prof.getName());

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
	
	public QuestingPacket getUpdateSyncPacket(UUID uuid, String name, boolean isOp)
	{
		NBTTagCompound jn = new NBTTagCompound();
		jn.setString("uuid", uuid.toString());
		jn.setString("name", name);
		jn.setBoolean("isOP", isOp);
		
		NBTTagList json = new NBTTagList();
		json.appendTag(jn);

		NBTTagCompound tags = new NBTTagCompound();
		tags.setBoolean("isUpdate", true);
		tags.setTag("data", json);
		return new QuestingPacket(PacketTypeNative.NAME_CACHE.GetLocation(), tags);
	}
	
	public void readUpdatePacket(NBTTagCompound payload)
	{
		readFromNBT(payload.getTagList("data", 10), EnumSaveType.CONFIG, false);
	}
	
	@Override
	public QuestingPacket getSyncPacket()
	{
		NBTTagCompound tags = new NBTTagCompound();
		tags.setTag("data", this.writeToNBT(new NBTTagList(), EnumSaveType.CONFIG));
		return new QuestingPacket(PacketTypeNative.NAME_CACHE.GetLocation(), tags);
	}
	
	/**
	 * Server -> Client packet about name updates
	 */
	@Override
	public void readPacket(NBTTagCompound payload)
	{
		readFromNBT(payload.getTagList("data", 10), EnumSaveType.CONFIG, payload.getBoolean("isUpdate"));
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

	/**
	 * Name cache import
	 */
	@Override
	public void readFromNBT(NBTTagList json, EnumSaveType saveType)
	{
		readFromNBT(json, saveType, false);
	}
	
	private void readFromNBT(NBTTagList json, EnumSaveType saveType, boolean updateOnly)
	{
		if(saveType != EnumSaveType.CONFIG)
		{
			return;
		}
		
		if (!updateOnly) cache.clear();
		
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
				
				if (updateOnly) removeUserFromCache(name);
				
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
	
	private void removeUserFromCache(String name)
	{
		UUID oldID = getUUID(name);
		
		while(oldID != null)
		{
			// Cleans out all name duplicates
			cache.remove(oldID);
			oldID = getUUID(name);
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
