package betterquesting.api.database;

import java.util.UUID;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import com.google.gson.JsonArray;

public interface INameCache extends IJsonSaveLoad<JsonArray>, IDataSync
{
	public String getName(UUID uuid);
	public UUID getUUID(String name);
	
	/**
	 * Used primarily to know if a user is an OP client side<br>
	 */
	public boolean isOP(UUID uuid);
	
	public void updateNames(MinecraftServer server);
	
	/**
	 * This should be used over the vanilla method of obtaining the player's UUID.
	 * This is because offline servers do not sync UUIDs properly
	 */
	public UUID getQuestingID(EntityPlayer player);
	
	public int size();
}
