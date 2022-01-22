package betterquesting.api2.storage;

import java.util.*;
import java.util.stream.Collectors;

class ArrayCacheLookupLogic<T> extends LookupLogic<T> {

    private DBEntry<T>[] cache = null;
    private int offset = -1;

    public ArrayCacheLookupLogic(SimpleDatabase<T> simpleDatabase) {
        super(simpleDatabase);
    }

    @Override
    public void onDataChange() {
        super.onDataChange();
        cache = null;
        offset = -1;
    }

    @Override
    public List<DBEntry<T>> getRefCache() {
        if (refCache != null) return refCache;
        if(cache == null) {
            return super.getRefCache();
        }
        else {
            refCache = Arrays.stream(cache).filter(Objects::nonNull).collect(Collectors.toList());
            return refCache;
        }
    }

    @Override
    public List<DBEntry<T>> bulkLookup(int[] keys) {
        computeCache();
        List<DBEntry<T>> list = new ArrayList<>(keys.length);
        for(int k : keys) {
            if (k - offset >= cache.length) continue;
            final DBEntry<T> element = cache[k - offset];
            if(element != null) {
                // it shouldn't place too much allocation/gc pressure since there aren't too many keys to look up anyway
                list.add(element);
            }
        }
        return list;
    }

    @SuppressWarnings("unchecked")
    private void computeCache() {
        if (cache != null) return;
        cache = new DBEntry[simpleDatabase.mapDB.lastKey() - simpleDatabase.mapDB.firstKey() + 1];
        offset = simpleDatabase.mapDB.firstKey();
        if(refCache == null) {
            for(Map.Entry<Integer, T> entry : simpleDatabase.mapDB.entrySet()) {
                cache[entry.getKey() - offset] = new DBEntry<>(entry.getKey(), entry.getValue());
            }
        } else {
            for(DBEntry<T> entry : refCache) {
                cache[entry.getID() - offset] = entry;
            }
        }
    }
}
