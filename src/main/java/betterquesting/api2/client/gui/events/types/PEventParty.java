package betterquesting.api2.client.gui.events.types;

import betterquesting.api2.client.gui.events.PanelEvent;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

// Use whenever one or more parties change
public class PEventParty extends PanelEvent {
    private final Set<Integer> partyIDs;

    public PEventParty(int partyID) {
        this.partyIDs = Collections.singleton(partyID);
    }

    public PEventParty(Collection<Integer> partyIDs) {
        this.partyIDs = Collections.unmodifiableSet(new TreeSet<>(partyIDs));
    }

    public Set<Integer> getPartyIDs() {
        return this.partyIDs;
    }

    @Override
    public boolean canCancel() {
        return false;
    }
}
