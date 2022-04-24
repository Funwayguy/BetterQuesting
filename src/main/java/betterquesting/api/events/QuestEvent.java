package betterquesting.api.events;

import net.minecraftforge.fml.common.eventhandler.Event;

import java.util.*;

public class QuestEvent extends Event {
    private final Type type;
    private final UUID playerID;
    private final Set<Integer> questIDs;

    public Set<Integer> getQuestIDs() {
        return this.questIDs;
    }

    public UUID getPlayerID() {
        return this.playerID;
    }

    public Type getType() {
        return this.type;
    }

    public QuestEvent(Type type, UUID playerID, int questID) {
        this.type = type;
        this.playerID = playerID;
        this.questIDs = Collections.singleton(questID);
    }

    public QuestEvent(Type type, UUID playerID, Collection<Integer> questIDs) {
        this.type = type;
        this.playerID = playerID;
        this.questIDs = Collections.unmodifiableSet(new TreeSet<>(questIDs));
    }

    public enum Type {
        COMPLETED,
        UPDATED,
        RESET
    }
}
