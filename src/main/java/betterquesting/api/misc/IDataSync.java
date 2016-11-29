package betterquesting.api.misc;

import net.minecraft.nbt.NBTTagCompound;
import betterquesting.api.network.QuestingPacket;

public interface IDataSync
{
	public QuestingPacket getSyncPacket();
	public void readPacket(NBTTagCompound payload);
}
