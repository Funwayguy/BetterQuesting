package adv_director.api.misc;

import net.minecraft.nbt.NBTTagCompound;
import adv_director.api.network.QuestingPacket;

public interface IDataSync
{
	public QuestingPacket getSyncPacket();
	public void readPacket(NBTTagCompound payload);
}
