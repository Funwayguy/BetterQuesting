package betterquesting.api.questing;

import betterquesting.api2.storage.IDatabaseNBT;
import net.minecraft.nbt.NBTTagList;
import betterquesting.api.misc.IDataSync;

public interface IQuestDatabase extends IDatabaseNBT<IQuest, NBTTagList, NBTTagList>, IDataSync
{
	IQuest createNew(int id);
}
