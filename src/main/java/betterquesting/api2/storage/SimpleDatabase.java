package betterquesting.api2.storage;

import java.util.*;
import java.util.Map.Entry;

public abstract class SimpleDatabase<T> implements IDatabase<T>
{
    private final TreeMap<Integer, T> mapDB = new TreeMap<>();
    
    private final BitSet idMap = new BitSet();
    private List<DBEntry<T>> refCache = null;
    
    @Override
    public synchronized int nextID()
    {
        return idMap.nextClearBit(0);
    }
    
    @Override
    public synchronized DBEntry<T> add(int id, T value)
    {
        if(value == null)
        {
            throw new NullPointerException("Value cannot be null");
        } else if(id < 0)
        {
            throw new IllegalArgumentException("ID cannot be negative");
        } else
        {
            if(mapDB.putIfAbsent(id, value) == null)
            {
                idMap.set(id);
                refCache = null;
                return new DBEntry<>(id, value);
            } else
            {
                throw new IllegalArgumentException("ID or value is already contained within database");
            }
        }
    }
    
    @Override
    public synchronized boolean removeID(int key)
    {
        if(key < 0) return false;
        
        if(mapDB.remove(key) != null)
        {
            idMap.clear(key);
            refCache = null;
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
    public synchronized T getValue(int id)
    {
        if(id < 0 || mapDB.size() <= 0) return null;
        return mapDB.get(id);
    }
    
    @Override
    public synchronized int size()
    {
        return mapDB.size();
    }
    
    @Override
    public synchronized void reset()
    {
        mapDB.clear();
        idMap.clear();
        refCache = Collections.emptyList();
    }
    
    @Override
    public synchronized List<DBEntry<T>> getEntries()
    {
        if(refCache == null)
        {
            List<DBEntry<T>> temp = new ArrayList<>();
            for(Entry<Integer,T> entry : mapDB.entrySet())
            {
                temp.add(new DBEntry<>(entry.getKey(), entry.getValue()));
            }
            refCache = Collections.unmodifiableList(temp);
        }
        
        return refCache;
    }
    
    @Override
    public synchronized List<DBEntry<T>> bulkLookup(int... keys)
    {
        if(keys.length <= 0) return Collections.emptyList();
        
        int[] sortedKeys = new int[keys.length];
        System.arraycopy(keys, 0, sortedKeys, 0, keys.length);
        Arrays.sort(sortedKeys);
        
        List<DBEntry<T>> subList = new ArrayList<>();
        int n = 0;
        
        for(DBEntry<T> entry : getEntries())
        {
            while(n < sortedKeys.length && sortedKeys[n] < entry.getID()) n++;
            if(n >= sortedKeys.length) break;
            if(sortedKeys[n] == entry.getID()) subList.add(entry);
        }
        
        return subList;
    }
}