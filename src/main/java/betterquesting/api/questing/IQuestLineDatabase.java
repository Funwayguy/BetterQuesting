package betterquesting.api.questing;

import betterquesting.api2.storage.DBEntry;
import betterquesting.api2.storage.IDatabase;
import betterquesting.api2.storage.INBTPartial;
import java.util.List;
import net.minecraft.nbt.NBTTagList;

public interface IQuestLineDatabase extends IDatabase<IQuestLine>, INBTPartial<NBTTagList, Integer> {
    IQuestLine createNew(int id);

    /**
     * Deletes quest from all quest lines
     */
    void removeQuest(int questID);

    int getOrderIndex(int lineID);

    void setOrderIndex(int lineID, int index);

    List<DBEntry<IQuestLine>> getSortedEntries();
}
