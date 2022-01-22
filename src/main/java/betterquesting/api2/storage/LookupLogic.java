package betterquesting.api2.storage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

abstract class LookupLogic<T> {

    protected final SimpleDatabase<T> simpleDatabase;
    protected List<DBEntry<T>> refCache = null;

    public LookupLogic(SimpleDatabase<T> simpleDatabase) {
        this.simpleDatabase = simpleDatabase;
    }

    public void onDataChange() {
        refCache = null;
    }

    public List<DBEntry<T>> getRefCache() {
        if (refCache != null) return refCache;
        computeRefCache();
        return refCache;
    }

    public abstract List<DBEntry<T>> bulkLookup(int[] keys);

    protected void computeRefCache() {
        List<DBEntry<T>> temp = new ArrayList<>();
        for(Map.Entry<Integer, T> entry : simpleDatabase.mapDB.entrySet()) {
            temp.add(new DBEntry<>(entry.getKey(), entry.getValue()));
        }
        refCache = Collections.unmodifiableList(temp);
    }
}
