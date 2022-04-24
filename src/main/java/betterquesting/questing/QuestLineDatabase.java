package betterquesting.questing;

import betterquesting.api.questing.IQuestLine;
import betterquesting.api.questing.IQuestLineDatabase;
import betterquesting.api2.storage.DBEntry;
import betterquesting.api2.storage.SimpleDatabase;
import betterquesting.api2.utils.QuestLineSorter;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.MathHelper;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public final class QuestLineDatabase extends SimpleDatabase<IQuestLine> implements IQuestLineDatabase {
    public static final QuestLineDatabase INSTANCE = new QuestLineDatabase();

    private final List<Integer> lineOrder = new ArrayList<>();
    private final QuestLineSorter SORTER = new QuestLineSorter(this);

    @Override
    public synchronized int getOrderIndex(int lineID) {
        int order = lineOrder.indexOf(lineID);
        if (order >= 0) return order;
        if (getValue(lineID) == null) return -1;

        lineOrder.add(lineID);
        return lineOrder.size() - 1;
    }

    @Override
    public synchronized void setOrderIndex(int lineID, int index) {
        lineOrder.remove((Integer) lineID);
        lineOrder.add(MathHelper.clamp(index, 0, lineOrder.size()), lineID);
    }

    @Override
    public synchronized List<DBEntry<IQuestLine>> getSortedEntries() {
        List<DBEntry<IQuestLine>> list = new ArrayList<>(this.getEntries());
        list.sort(SORTER);
        return list;
    }

    @Override
    public synchronized IQuestLine createNew(int id) {
        IQuestLine ql = new QuestLine();
        if (id >= 0) this.add(id, ql);
        return ql;
    }

    @Override
    public synchronized void removeQuest(int questID) {
        for (DBEntry<IQuestLine> ql : getEntries()) {
            ql.getValue().removeID(questID);
        }
    }

    @Override
    public synchronized NBTTagList writeToNBT(NBTTagList json, @Nullable List<Integer> subset) {
        for (DBEntry<IQuestLine> entry : getEntries()) {
            if (subset != null && !subset.contains(entry.getID())) continue;
            NBTTagCompound jObj = entry.getValue().writeToNBT(new NBTTagCompound(), null);
            jObj.setInteger("lineID", entry.getID());
            jObj.setInteger("order", getOrderIndex(entry.getID()));
            json.appendTag(jObj);
        }

        return json;
    }

    @Override
    public synchronized void readFromNBT(NBTTagList json, boolean merge) {
        if (!merge) reset();

        List<IQuestLine> unassigned = new ArrayList<>();
        HashMap<Integer, Integer> orderMap = new HashMap<>();

        for (int i = 0; i < json.tagCount(); i++) {
            NBTTagCompound jql = json.getCompoundTagAt(i);

            int id = jql.hasKey("lineID", 99) ? jql.getInteger("lineID") : -1;
            int order = jql.hasKey("order", 99) ? jql.getInteger("order") : -1;

            IQuestLine line = getValue(id);
            if (line == null) line = new QuestLine();
            line.readFromNBT(jql, false);

            if (id >= 0) {
                add(id, line);
            } else {
                unassigned.add(line);
            }

            if (order >= 0) orderMap.put(order, id);
        }

        // Legacy support ONLY
        for (IQuestLine q : unassigned) add(nextID(), q);

        List<Integer> orderKeys = new ArrayList<>(orderMap.keySet());
        Collections.sort(orderKeys);

        lineOrder.clear();
        for (int o : orderKeys) lineOrder.add(orderMap.get(o));
    }

    @Override
    public synchronized void reset() {
        super.reset();
        lineOrder.clear();
    }
}