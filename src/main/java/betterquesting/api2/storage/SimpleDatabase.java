package betterquesting.api2.storage;

import java.util.*;
import java.util.Map.Entry;

public abstract class SimpleDatabase<T> implements IDatabase<T>
{
    private final SortedMap<Integer, T> mapDB = Collections.synchronizedSortedMap(new TreeMap<>());
    
    private final BitSet idMap = new BitSet();
    private DBEntry<T>[] refCache = null; // TODO: Change out to an unmodifiable list
    
    @Override
    public int nextID()
    {
        synchronized(mapDB)
        {
            return idMap.nextClearBit(0);
        }
    }
    
    @Override
    public DBEntry<T> add(int id, T value)// throws NullPointerException, IllegalArgumentException // TODO: Enforce this
    {
        synchronized(mapDB)
        {
            if(value == null)
            {
                throw new NullPointerException("Value cannot be null");
            } else if(id < 0)
            {
                throw new IllegalArgumentException("ID cannot be negative");
            } else
            {
                if(!idMap.get(id) && !mapDB.containsValue(value))
                {
                    mapDB.put(id, value);
                    idMap.set(id);
                    refCache = null;
                    return new DBEntry<>(id, value);
                } else
                {
                    throw new IllegalArgumentException("ID or value is already contained within database");
                }
            }
        }
    }
    
    @Override
    public boolean removeID(int key)
    {
        if(key < 0) return false;
        
        synchronized(mapDB)
        {
            if(mapDB.remove(key) != null)
            {
                idMap.clear(key);
                refCache = null;
                return true;
            }
        }
        
        return false;
    }
    
    @Override
    public boolean removeValue(T value)
    {
        if(value == null) return false;
        
        synchronized(mapDB)
        {
            Iterator<Entry<Integer,T>> iter = mapDB.entrySet().iterator();
            
            while(iter.hasNext())
            {
                Entry<Integer,T> entry = iter.next();
                if(entry.getValue() == value)
                {
                    iter.remove();
                    idMap.clear(entry.getKey());
                    refCache = null;
                    return true;
                }
            }
        }
        
        return false;
    }
    
    @Override
    public int getID(T value)
    {
        if(value == null) return -1;
        
        for(DBEntry<T> entry : getEntries())
        {
            if(entry.getValue() == value) return entry.getID();
        }
        
        return -1;
    }
    
    @Override
    public T getValue(int id)
    {
        synchronized(mapDB)
        {
            if(id < 0 || mapDB.size() <= 0 || !idMap.get(id)) return null;
            return mapDB.get(id);
        }
    }
    
    @Override
    public int size()
    {
        return mapDB.size();
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public void reset()
    {
        synchronized(mapDB)
        {
            mapDB.clear();
            idMap.clear();
            refCache = new DBEntry[0];
        }
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public DBEntry<T>[] getEntries() // TODO: Change out to an unmodifiable list
    {
        synchronized(mapDB)
        {
            if(refCache == null)
            {
                refCache = new DBEntry[mapDB.size()];
                int i = 0;
                for(Entry<Integer,T> entry : mapDB.entrySet())
                {
                    refCache[i++] = new DBEntry<>(entry.getKey(), entry.getValue());
                }
            }
            return refCache;
        }
    }
}