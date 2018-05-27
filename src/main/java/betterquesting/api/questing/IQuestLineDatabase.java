package betterquesting.api.questing;

import betterquesting.api2.storage.DBEntry;
import betterquesting.api2.storage.IDatabase;
import net.minecraft.nbt.NBTTagList;
import betterquesting.api.misc.IDataSync;
import betterquesting.api.misc.INBTSaveLoad;

public interface IQuestLineDatabase extends IDatabase<IQuestLine>, INBTSaveLoad<NBTTagList>, IDataSync
{
	IQuestLine createNew(int id);
	
	/**
	 * Deletes quest from all quest lines
	 */
	void removeQuest(int questID);
	
	int getOrderIndex(int lineID);
	void setOrderIndex(int lineID, int index);
	
	DBEntry<IQuestLine>[] getSortedEntries();
	
}
