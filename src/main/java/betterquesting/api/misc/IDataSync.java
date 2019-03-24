package betterquesting.api.misc;

import net.minecraft.nbt.NBTTagCompound;
import betterquesting.api.network.QuestingPacket;

public interface IDataSync
{
	QuestingPacket getSyncPacket();
	void readPacket(NBTTagCompound payload);
}
