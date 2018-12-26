package betterquesting.api.storage;

import java.util.List;
import java.util.UUID;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.server.MinecraftServer;
import betterquesting.api.misc.IDataSync;
import betterquesting.api.misc.INBTSaveLoad;

public interface INameCache extends INBTSaveLoad<NBTTagList>, IDataSync
{
	public String getName(UUID uuid);
	public UUID getUUID(String name);
	public UUID registerAndGetUUID(EntityPlayer player);
	
	public List<String> getAllNames();
	
	/**
	 * Used primarily to know if a user is an OP client side<br>
	 */
	public boolean isOP(UUID uuid);
	
	public void updateNames(MinecraftServer server);
	
	public int size();
}
