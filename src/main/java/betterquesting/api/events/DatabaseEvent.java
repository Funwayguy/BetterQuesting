package betterquesting.api.events;

import net.minecraftforge.fml.common.eventhandler.Event;

/**
 * Fired when the whole questing database for world is modified, loaded or saved.
 * Can be used to save/load custom databases in expansions or update dependent GUIs
 */
// TODO: Replace with a better system. Stop using this for updating screens too
@Deprecated
public abstract class DatabaseEvent extends Event {
    private final DBType TYPE;

    public DatabaseEvent(DBType type) {
        this.TYPE = type;
    }

    public DBType getType() {
        return this.TYPE;
    }

    @Deprecated
    public static class Update extends DatabaseEvent {
        public Update(DBType type) {
            super(type);
        }
    }

    @Deprecated
    public static class Load extends DatabaseEvent {
        public Load(DBType type) {
            super(type);
        }
    }

    @Deprecated
    public static class Save extends DatabaseEvent {
        public Save(DBType type) {
            super(type);
        }
    }

    public enum DBType {
        QUEST,
        CHAPTER,
        PARTY,
        NAMES,
        ALL,
        OTHER
    }
}
