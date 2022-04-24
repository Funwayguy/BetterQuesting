package betterquesting.api2.client.gui.events.types;

import betterquesting.api2.client.gui.events.PanelEvent;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

// Use whenever one or more quests change
public class PEventQuest extends PanelEvent {
    private final Set<Integer> questIDs;

    public PEventQuest(int questID) {
        this.questIDs = Collections.singleton(questID);
    }

    public PEventQuest(Collection<Integer> questIDs) {
        this.questIDs = Collections.unmodifiableSet(new TreeSet<>(questIDs));
    }

    public Set<Integer> getQuestID() {
        return this.questIDs;
    }

    @Override
    public boolean canCancel() {
        return false;
    }
}
