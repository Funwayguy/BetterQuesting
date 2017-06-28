package adv_director.api.storage;

import java.util.List;
import java.util.UUID;
import net.minecraft.server.MinecraftServer;
import adv_director.api.misc.IDataSync;
import adv_director.api.misc.IJsonSaveLoad;
import com.google.gson.JsonArray;

public interface INameCache extends IJsonSaveLoad<JsonArray>, IDataSync
{
	public String getName(UUID uuid);
	public UUID getUUID(String name);
	
	public List<String> getAllNames();
	
	/**
	 * Used primarily to know if a user is an OP client side<br>
	 */
	public boolean isOP(UUID uuid);
	
	public void updateNames(MinecraftServer server);
	
	public int size();
}
