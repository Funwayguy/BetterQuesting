package betterquesting.api.api;

import net.minecraft.entity.player.EntityPlayer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.UUID;

/**
 * Storage for all the questing API hooks. Includes built-in and custom APIs
 */
public class QuestingAPI {
  private static final HashMap<ApiKey<?>, Object> apis = new HashMap<>();

  public static <T> void registerAPI(ApiKey<T> key, T api) {
    if (key == null) {
      throw new NullPointerException("API key can not be NULL");
    } else if (api == null) {
      throw new NullPointerException("Tried to registed NULL API");
    } else if (apis.containsKey(key)) {
      throw new IllegalArgumentException("Key cannot be registered twice");
    }

    apis.put(key, api);
  }

  @SuppressWarnings("unchecked")
  public static <T> T getAPI(ApiKey<T> key) {
    Object obj = apis.get(key);
    return obj == null ? null : (T) obj;
  }

  /**
   * This should be used over the vanilla method of obtaining the player's UUID.
   * This is because offline servers do not sync UUIDs properly to the client
   */
  public static UUID getQuestingUUID(EntityPlayer player) {
    if (player == null) {
      return null;
    }

    if (player.world.isRemote) {
      UUID uuid = getAPI(ApiReference.NAME_CACHE).getUUID(player.getGameProfile().getName());

      if (uuid != null) {
        return uuid;
      }
    }

    return player.getGameProfile().getId();
  }

  private static Logger logger = null;

  public static Logger getLogger() {
    if (logger == null) {
      logger = LogManager.getLogger("betterquesting");
    }

    return logger;
  }
}
