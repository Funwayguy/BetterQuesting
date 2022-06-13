package betterquesting.api.events;

import net.minecraftforge.fml.common.eventhandler.Event;

import java.util.Collection;
import java.util.Collections;
import java.util.TreeSet;
import java.util.UUID;

public class MarkDirtyPlayerEvent extends Event {

    private final Collection<UUID> dirtyPlayerIDs;

    public MarkDirtyPlayerEvent(UUID dirtyPlayerID) {
        if (dirtyPlayerID == null) {
            this.dirtyPlayerIDs = Collections.emptySet();
        } else {
            this.dirtyPlayerIDs = Collections.singleton(dirtyPlayerID);
        }
    }

    public MarkDirtyPlayerEvent(Collection<UUID> dirtyPlayerIDs) {
        if (dirtyPlayerIDs == null) {
            this.dirtyPlayerIDs = Collections.emptySet();
        } else {
            this.dirtyPlayerIDs = Collections.unmodifiableCollection(new TreeSet<>(dirtyPlayerIDs));
        }
    }

    public Collection<UUID> getDirtyPlayerIDs() {
        return dirtyPlayerIDs;
    }
}
