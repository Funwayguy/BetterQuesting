package betterquesting.api.questing;

import betterquesting.api.misc.IDataSync;
import betterquesting.api2.storage.DBEntry;
import betterquesting.api2.storage.IDatabase;
import betterquesting.api2.storage.INBTPartial;
import betterquesting.api2.storage.INBTProgress;
import net.minecraft.nbt.NBTTagList;

import java.util.List;

public interface IQuestDatabase extends IDatabase<IQuest>, INBTPartial<NBTTagList>, INBTProgress<NBTTagList>, IDataSync
{
	IQuest createNew(int id);
	List<DBEntry<IQuest>> bulkLookup(int... ids);
}
