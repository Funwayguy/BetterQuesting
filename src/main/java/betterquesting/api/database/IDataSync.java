package betterquesting.api.database;

import net.minecraft.nbt.NBTTagCompound;
import betterquesting.api.network.PreparedPayload;

public interface IDataSync
{
	public PreparedPayload getSyncPacket();
	public void readPacket(NBTTagCompound payload);
}
