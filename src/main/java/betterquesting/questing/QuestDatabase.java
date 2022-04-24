package betterquesting.questing;

import betterquesting.api.questing.IQuest;
import betterquesting.api.questing.IQuestDatabase;
import betterquesting.api2.storage.DBEntry;
import betterquesting.api2.storage.SimpleDatabase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

public final class QuestDatabase extends SimpleDatabase<IQuest> implements IQuestDatabase {
    public static final QuestDatabase INSTANCE = new QuestDatabase();

    @Override
    public synchronized IQuest createNew(int id) {
        IQuest quest = new QuestInstance();
        if (id >= 0) this.add(id, quest);
        return quest;
    }

    @Override
    public synchronized boolean removeID(int id) {
        boolean success = super.removeID(id);
        if (success) for (DBEntry<IQuest> entry : getEntries()) removeReq(entry.getValue(), id);
        return success;
    }

    @Override
    public synchronized boolean removeValue(IQuest value) {
        int id = this.getID(value);
        if (id < 0) return false;
        boolean success = this.removeValue(value);
        if (success) for (DBEntry<IQuest> entry : getEntries()) removeReq(entry.getValue(), id);
        return success;
    }

    private void removeReq(IQuest quest, int id) {
        int[] orig = quest.getRequirements();
        if (orig.length <= 0) return;
        boolean hasRemoved = false;
        int[] rem = new int[orig.length - 1];
        for (int i = 0; i < orig.length; i++) {
            if (!hasRemoved && orig[i] == id) {
                hasRemoved = true;
                continue;
            } else if (!hasRemoved && i >= rem.length) break;

            rem[!hasRemoved ? i : (i - 1)] = orig[i];
        }

        if (hasRemoved) quest.setRequirements(rem);
    }

    @Override
    public synchronized NBTTagList writeToNBT(NBTTagList json, @Nullable List<Integer> subset) {
        for (DBEntry<IQuest> entry : this.getEntries()) {
            if (subset != null && !subset.contains(entry.getID())) continue;
            NBTTagCompound jq = entry.getValue().writeToNBT(new NBTTagCompound());
            if (subset != null && jq.isEmpty()) continue;
            jq.setInteger("questID", entry.getID());
            json.appendTag(jq);
        }

        return json;
    }

    @Override
    public synchronized void readFromNBT(NBTTagList nbt, boolean merge) {
        if (!merge) this.reset();

        for (int i = 0; i < nbt.tagCount(); i++) {
            NBTTagCompound qTag = nbt.getCompoundTagAt(i);

            int qID = qTag.hasKey("questID", 99) ? qTag.getInteger("questID") : -1;
            if (qID < 0) continue;

            IQuest quest = getValue(qID);
            if (quest == null) quest = this.createNew(qID);
            quest.readFromNBT(qTag);
        }
    }

    @Override
    public synchronized NBTTagList writeProgressToNBT(NBTTagList json, @Nullable List<UUID> users) {
        for (DBEntry<IQuest> entry : this.getEntries()) {
            NBTTagCompound jq = entry.getValue().writeProgressToNBT(new NBTTagCompound(), users);
            jq.setInteger("questID", entry.getID());
            json.appendTag(jq);
        }

        return json;
    }

    @Override
    public synchronized void readProgressFromNBT(NBTTagList json, boolean merge) {
        for (int i = 0; i < json.tagCount(); i++) {
            NBTTagCompound qTag = json.getCompoundTagAt(i);

            int qID = qTag.hasKey("questID", 99) ? qTag.getInteger("questID") : -1;
            if (qID < 0) continue;

            IQuest quest = getValue(qID);
            if (quest != null) quest.readProgressFromNBT(qTag, merge);
        }
    }
}
