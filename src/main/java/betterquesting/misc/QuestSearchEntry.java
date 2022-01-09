package betterquesting.misc;

import betterquesting.api.questing.IQuest;
import betterquesting.api.questing.IQuestLine;
import betterquesting.api2.storage.DBEntry;

public class QuestSearchEntry {
    public QuestSearchEntry(DBEntry<IQuest> quest, DBEntry<IQuestLine> questLineEntry) {
        this.quest = quest;
        this.questLineEntry = questLineEntry;
    }

    private DBEntry<IQuest> quest;

    public DBEntry<IQuest> getQuest() {
        return quest;
    }

    public void setQuest(DBEntry<IQuest> quest) {
        this.quest = quest;
    }

    public DBEntry<IQuestLine> getQuestLineEntry() {
        return questLineEntry;
    }

    public void setQuestLineEntry(DBEntry<IQuestLine> questLineEntry) {
        this.questLineEntry = questLineEntry;
    }

    private DBEntry<IQuestLine> questLineEntry;
}
