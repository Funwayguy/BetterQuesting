package betterquesting.api2.storage;

import java.util.*;
import java.util.Map.Entry;

public abstract class SimpleDatabase<T> implements IDatabase<T>
{
    private final TreeMap<Integer, T> mapDB = new TreeMap<>();
    
    private final BitSet idMap = new BitSet();
    private DBEntry<T>[] refCache = null; // TODO: Change out to an unmodifiable list
    
    @Override
    public synchronized int nextID()
    {
        return idMap.nextClearBit(0);
    }
    
    @Override
    public synchronized DBEntry<T> add(int id, T value)// throws NullPointerException, IllegalArgumentException // TODO: Enforce this
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
    @SuppressWarnings("unchecked")
    public synchronized void reset()
    {
        mapDB.clear();
        idMap.clear();
        refCache = new DBEntry[0];
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public synchronized DBEntry<T>[] getEntries() // TODO: Change out to an unmodifiable list
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