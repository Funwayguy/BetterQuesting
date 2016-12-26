package betterquesting.api.api;

import java.util.HashMap;
import java.util.UUID;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import net.minecraft.entity.player.EntityPlayer;

/**
 * Storage for all the questing API hooks. Includes built-in and custom APIs
 */
public class QuestingAPI
{
	private static final HashMap<ApiKey<?>, Object> apis = new HashMap<ApiKey<?>, Object>();
	
	public static <T> void registerAPI(ApiKey<T> key, T api)
	{
		if(key == null)
		{
			throw new NullPointerException("API key can not be NULL");
		} else if(api == null)
		{
			throw new NullPointerException("Tried to registed NULL API");
		} else if(apis.containsKey(key))
		{
			throw new IllegalArgumentException("Key cannot be registered twice");
		}
		
		apis.put(key, api);
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T getAPI(ApiKey<T> key)
	{
		Object obj = apis.get(key);
		
		if(obj == null)
		{
			return null;
		} else
		{
			return (T)obj;
		}
	}
	
	/**
	 * This should be used over the vanilla method of obtaining the player's UUID.
	 * This is because offline servers do not sync UUIDs properly to the client
	 */
	public static UUID getQuestingUUID(EntityPlayer player)
	{
		if(player == null)
		{
			return null;
		}
		
		if(player.worldObj.isRemote)
		{
			UUID uuid = getAPI(ApiReference.NAME_CACHE).getUUID(player.getGameProfile().getName());
			
			if(uuid != null)
			{
				return uuid;
			}
		}
		
		return player.getGameProfile().getId();
	}
	
	private static Logger logger = null;
	
	public static Logger getLogger()
	{
		if(logger == null)
		{
			logger = LogManager.getLogger("betterquesting");
		}
		
		return logger;
	}
}
