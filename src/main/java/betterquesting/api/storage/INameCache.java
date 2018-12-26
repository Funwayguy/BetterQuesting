package betterquesting.api.storage;

import betterquesting.api.misc.IDataSync;
import betterquesting.api.misc.INBTSaveLoad;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.server.MinecraftServer;

import java.util.List;
import java.util.UUID;

public interface INameCache extends INBTSaveLoad<NBTTagList>, IDataSync
{
    void setName(UUID uuid, String name);
	String getName(UUID uuid);
	UUID getUUID(String name);
	
	List<String> getAllNames();
	
	/**
	 * Used primarily to know if a user is an OP client side<br>
	 */
	boolean isOP(UUID uuid);
	
	void updateNames(MinecraftServer server);
	
	int size();
}
