package betterquesting.api.other;

import net.minecraft.nbt.NBTTagCompound;
import betterquesting.api.network.QuestingPacket;

public interface IDataSync
{
	public QuestingPacket getSyncPacket();
	public void readPacket(NBTTagCompound payload);
}
