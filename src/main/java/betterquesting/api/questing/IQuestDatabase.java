package betterquesting.api.questing;

import betterquesting.api.misc.IDataSync;
import betterquesting.api2.storage.IDatabase;
import betterquesting.api2.storage.INBTPartial;
import betterquesting.api2.storage.INBTProgress;
import net.minecraft.nbt.NBTTagList;

public interface IQuestDatabase extends IDatabase<IQuest>, INBTPartial<NBTTagList>, INBTProgress<NBTTagList>, IDataSync
{
	IQuest createNew(int id);
}
