package betterquesting.api2.client.gui.panels.lists;

import betterquesting.api.properties.NativeProps;
import betterquesting.api.questing.IQuest;
import betterquesting.api.questing.IQuestLine;
import betterquesting.api2.client.gui.misc.IGuiRect;
import betterquesting.api2.client.gui.panels.lists.CanvasSearch;
import betterquesting.api2.storage.DBEntry;
import betterquesting.api2.utils.QuestTranslation;
import betterquesting.misc.QuestSearchEntry;
import betterquesting.questing.QuestDatabase;
import betterquesting.questing.QuestLineDatabase;

import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public abstract class CanvasQuestSearch extends CanvasSearch<QuestSearchEntry, QuestSearchEntry> {
    private List<QuestSearchEntry> questList;

    public CanvasQuestSearch(IGuiRect rect) {
        super(rect);
    }

    @Override
    protected Iterator<QuestSearchEntry> getIterator() {
        if (questList == null){
            questList = QuestLineDatabase.INSTANCE.getEntries().stream().flatMap(iQuestLineDBEntry -> {
                IQuestLine value = iQuestLineDBEntry.getValue();
                return value.getEntries().stream().map(iQuestLineEntryDBEntry ->
                        new QuestSearchEntry(new DBEntry<>(iQuestLineEntryDBEntry.getID(), QuestDatabase.INSTANCE.getValue(iQuestLineEntryDBEntry.getID())),
                                iQuestLineDBEntry)
                );
            }).collect(Collectors.toList());
        }
        return questList.iterator();
    }

    @Override
    protected void queryMatches(QuestSearchEntry value, String query, ArrayDeque<QuestSearchEntry> results) {
        if (("" + value.getQuest().getID()).contains(query) ||
                value.getQuest().getValue().getProperty(NativeProps.NAME).toLowerCase().contains(query) ||
                QuestTranslation.translate(value.getQuest().getValue().getProperty(NativeProps.NAME)).toLowerCase().contains(query)) {
            results.add(value);
        }
    }
}