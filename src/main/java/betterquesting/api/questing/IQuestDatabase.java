package betterquesting.api.questing;

import betterquesting.api2.storage.DBEntry;
import betterquesting.api2.storage.IDatabase;
import betterquesting.api2.storage.INBTPartial;
import betterquesting.api2.storage.INBTProgress;
import net.minecraft.nbt.ListNBT;

import java.util.List;

public interface IQuestDatabase extends IDatabase<IQuest>, INBTPartial<ListNBT, Integer>, INBTProgress<ListNBT>
{
	IQuest createNew(int id);
	List<DBEntry<IQuest>> bulkLookup(int... ids);
}
