package betterquesting.api2.utils;

import betterquesting.api.questing.IQuestLine;
import betterquesting.api.questing.IQuestLineDatabase;
import betterquesting.api2.storage.DBEntry;

import java.util.Comparator;

public class QuestLineSorter implements Comparator<DBEntry<IQuestLine>> {
    private final IQuestLineDatabase QL_DB;

    public QuestLineSorter(IQuestLineDatabase database) {
        this.QL_DB = database;
    }

    @Override
    public int compare(DBEntry<IQuestLine> objA, DBEntry<IQuestLine> objB) {
        return Integer.compare(QL_DB.getOrderIndex(objA.getID()), QL_DB.getOrderIndex(objB.getID()));
    }
}
