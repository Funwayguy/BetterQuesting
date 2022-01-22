package betterquesting.api2.storage;

import java.util.*;
import java.util.Map.Entry;

public class SimpleDatabase<T> implements IDatabase<T> {

    /**
     * If the cache size would somehow exceed 24MB (on 64bit machines) we stop.
     */
    public static int CACHE_MAX_SIZE = 24 * 1024 * 1024 / 8;

    /**
     * If {@code mapDB.size < SPARSE_RATIO * (mapDB.lastKey() - mapDB.firstKey())} the database will be considered
     * sparse and an cache array won't be built to save memory.
     * <p>
     * Under this sparsity a 10k element database will roughly result in a 0.5MB cache which is more than enough reasonable.
     */
    public static double SPARSE_RATIO = 0.15d;

    final TreeMap<Integer, T> mapDB = new TreeMap<>();

    private final BitSet idMap = new BitSet();
    private LookupLogicType type = null;
    private LookupLogic<T> logic = null;

    private LookupLogic<T> getLookupLogic() {
        if (type != null) return logic;
        LookupLogicType newType = LookupLogicType.determine(this);
        type = newType;
        logic = newType.get(this);
        return logic;
    }

    private void updateLookupLogic() {
        if (type == null) return;
        LookupLogicType newType = LookupLogicType.determine(this);
        if (newType != type) {
            type = null;
            logic = null;
        } else {
            logic.onDataChange();
        }
    }

    @Override
    public synchronized int nextID() {
        return idMap.nextClearBit(0);
    }

    @Override
    public synchronized DBEntry<T> add(int id, T value) {
        if(value == null) {
            throw new NullPointerException("Value cannot be null");
        } else if(id < 0) {
            throw new IllegalArgumentException("ID cannot be negative");
        } else {
            if(mapDB.putIfAbsent(id, value) == null) {
                idMap.set(id);
                updateLookupLogic();
                return new DBEntry<>(id, value);
            } else {
                throw new IllegalArgumentException("ID or value is already contained within database");
            }
        }
    }

    @Override
    public synchronized boolean removeID(int key) {
        if(key < 0) return false;

        if(mapDB.remove(key) != null) {
            idMap.clear(key);
            updateLookupLogic();
            return true;
        }
        return false;
    }
    
    @Override
    public synchronized boolean removeValue(T value)
    {
        return value != null && removeID(getID(value));
    }
    
    @Override
    public synchronized int getID(T value)
    {
        if(value == null) return -1;
        
        for(DBEntry<T> entry : getEntries())
        {
            if(entry.getValue() == value) return entry.getID();
        }
        
        return -1;
    }

    @Override
    public synchronized T getValue(int id) {
        if(id < 0 || mapDB.size() <= 0) return null;
        return mapDB.get(id);
    }

    @Override
    public synchronized int size() {
        return mapDB.size();
    }

    @Override
    public synchronized void reset() {
        mapDB.clear();
        idMap.clear();
        type = null;
        logic = null;
    }

    @Override
    public synchronized List<DBEntry<T>> getEntries() {
        return mapDB.isEmpty() ? Collections.emptyList() : getLookupLogic().getRefCache();
    }

    /**
     * First try to use array cache.
     * If memory usage would be too high try use sort merge join if keys is large.
     * Otherwise look up each key separately via {@link TreeMap#get(Object)}.
     */
    @Override
    public synchronized List<DBEntry<T>> bulkLookup(int... keys) {
        return mapDB.isEmpty() || keys.length == 0 ? Collections.emptyList() : getLookupLogic().bulkLookup(keys);
    }
}
