package betterquesting.client.importers;

import betterquesting.api.questing.IQuest;
import betterquesting.api.questing.IQuestDatabase;
import betterquesting.api2.storage.DBEntry;
import betterquesting.api2.storage.SimpleDatabase;
import betterquesting.questing.QuestInstance;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class ImportedQuests extends SimpleDatabase<IQuest> implements IQuestDatabase {
    @Override
    public IQuest createNew(int id) {
        return this.add(id, new QuestInstance()).getValue();
    }

    @Override
    public List<DBEntry<IQuest>> bulkLookup(int... ids) {
        if (ids == null || ids.length <= 0) return Collections.emptyList();

        List<DBEntry<IQuest>> values = new ArrayList<>();

        synchronized (this) {
            for (int i : ids) {
                IQuest v = getValue(i);
                if (v != null) values.add(new DBEntry<>(i, v));
            }
        }

        return values;
    }

    @Override
    public NBTTagList writeToNBT(NBTTagList nbt, List<Integer> subset) {
        for (DBEntry<IQuest> entry : this.getEntries()) {
            if (subset != null && !subset.contains(entry.getID())) continue;
            NBTTagCompound jq = new NBTTagCompound();
            entry.getValue().writeToNBT(jq);
            jq.setInteger("questID", entry.getID());
            nbt.appendTag(jq);
        }

        return nbt;
    }

    @Override
    public void readFromNBT(NBTTagList nbt, boolean merge) {
        if (!merge) this.reset();

        for (int i = 0; i < nbt.tagCount(); i++) {
            NBTTagCompound qTag = nbt.getCompoundTagAt(i);

            int qID = qTag.hasKey("questID", 99) ? qTag.getInteger("questID") : -1;
            if (qID < 0) continue;

            IQuest quest = getValue(qID);
            quest = quest != null ? quest : this.createNew(qID);
            quest.readFromNBT(qTag);
        }
    }

    @Override
    public NBTTagList writeProgressToNBT(NBTTagList nbt, @Nullable List<UUID> users) {
        return nbt;
    }

    @Override
    public void readProgressFromNBT(NBTTagList nbt, boolean merge) {
    }
}
