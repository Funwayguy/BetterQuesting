package betterquesting.api2.client.gui.panels.lists;

import betterquesting.api.properties.NativeProps;
import betterquesting.api.questing.IQuest;
import betterquesting.api2.client.gui.misc.IGuiRect;
import betterquesting.api2.storage.DBEntry;
import betterquesting.api2.utils.QuestTranslation;
import betterquesting.questing.QuestDatabase;

import java.util.ArrayDeque;
import java.util.Iterator;

@SuppressWarnings("WeakerAccess")
public abstract class CanvasQuestDatabase extends CanvasSearch<DBEntry<IQuest>, DBEntry<IQuest>> {
    public CanvasQuestDatabase(IGuiRect rect) {
        super(rect);
    }

    @Override
    protected Iterator<DBEntry<IQuest>> getIterator() {
        return QuestDatabase.INSTANCE.getEntries().iterator();
    }

    @Override
    protected void queryMatches(DBEntry<IQuest> entry, String query, final ArrayDeque<DBEntry<IQuest>> results) {
        if (("" + entry.getID()).contains(query) || entry.getValue().getProperty(NativeProps.NAME).toLowerCase().contains(query) || QuestTranslation.translate(entry.getValue().getProperty(NativeProps.NAME)).toLowerCase().contains(query)) {
            results.add(entry);
        }
    }
}
