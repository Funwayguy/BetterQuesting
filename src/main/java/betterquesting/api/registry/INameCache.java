package betterquesting.api.registry;

import java.util.UUID;
import net.minecraft.server.MinecraftServer;
import betterquesting.api.other.IDataSync;
import betterquesting.api.other.IJsonSaveLoad;
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
	
	public int size();
}
