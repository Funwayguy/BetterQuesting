package betterquesting.api2.storage;

import java.util.*;
import java.util.Map.Entry;

// Divides the database into smaller indexed blocks to speed up search times.
public abstract class BigDatabase<T> implements IDatabase<T>
{
    private final SortedMap<Integer,SortedMap<Integer,T>> dbBlockMap = Collections.synchronizedSortedMap(new TreeMap<>());
    
    private final BitSet idMap = new BitSet();
    private DBEntry<T>[] refCache = null;
    
    private final int blockSize; // The size of indexed blocks
    
    public BigDatabase()
    {
        this(100);
    }
    
    public BigDatabase(int blockSize)
    {
        this.blockSize = blockSize <= 0 ? 1 : blockSize;
    }
    
    @Override
    public int nextID()
    {
        synchronized(dbBlockMap)
        {
            return idMap.nextClearBit(0);
        }
    }
    
    @Override
    public DBEntry<T> add(int id, T value)// throws NullPointerException, IllegalArgumentException // TODO: Enforce this
    {
        synchronized(dbBlockMap)
        {
            if(value == null)
            {
                throw new NullPointerException("Value cannot be null");
            } else if(id < 0)
            {
                throw new IllegalArgumentException("ID cannot be negative");
            } else if(idMap.get(id))
            {
                throw new IllegalArgumentException("ID is already contained within database");
            }
            
            int blockID = id / blockSize;
            
            SortedMap<Integer,T> blockEntry = dbBlockMap.get(blockID);
            if(blockEntry == null)
            {
                blockEntry = new TreeMap<>();
                dbBlockMap.put(blockID, blockEntry);
            } else if(blockEntry.containsValue(value)) // NOTE: This only works within the given block.
            {
                throw new IllegalArgumentException("Value is already contained within database");
            }
            
            blockEntry.put(id, value);
            idMap.set(id);
            refCache = null;
            return new DBEntry<>(id, value);
        }
    }
    
    @Override
    public boolean removeID(int id)
    {
        synchronized(dbBlockMap)
        {
            int blockID = id / blockSize;
            SortedMap<Integer,T> blockEntry = dbBlockMap.get(blockID);
            if(blockEntry == null) return false;
            if(blockEntry.remove(id) != null)
            {
                if(blockEntry.size() <= 0) dbBlockMap.remove(blockID);
                idMap.clear(id);
                refCache = null;
                return true;
            }
        }
        
        return false;
    }
    
    @Override
    public boolean removeValue(T value)
    {
        synchronized(dbBlockMap)
        {
            Iterator<SortedMap<Integer,T>> iterBlock = dbBlockMap.values().iterator();
            
            while(iterBlock.hasNext())
            {
                SortedMap<Integer,T> blockEntry = iterBlock.next();
                Iterator<Entry<Integer,T>> iterInner = blockEntry.entrySet().iterator();
                while(iterInner.hasNext())
                {
                    Entry<Integer,T> entry = iterInner.next();
                    if(entry.getValue() == value)
                    {
                        iterInner.remove();
                        idMap.clear(entry.getKey());
                        refCache = null;
                        if(blockEntry.size() <= 0) iterBlock.remove();
                        return true;
                    }
                }
            }
            
            return false;
        }
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
        synchronized(dbBlockMap)
        {
            if(id < 0 || dbBlockMap.size() <= 0 || !idMap.get(id)) return null;
            int blockID = id / blockSize;
            SortedMap<Integer,T> blockEntry = dbBlockMap.get(blockID);
            if(blockEntry == null) return null;
            return blockEntry.get(id);
        }
    }
    
    /**
     * Unlike getValue(), this method can retrieve a whole set of entries in one pass instead of one for every ID
     * @param ids array of IDs to search for
     * @return List of database entries that match the provided IDs
     */
    public List<DBEntry<T>> bulkLookup(int... ids)
    {
        if(ids == null || ids.length <= 0) return Collections.emptyList();
        
        List<DBEntry<T>> values = new ArrayList<>();
        
        synchronized(dbBlockMap)
        {
            for(int i : ids)
            {
                T v = getValue(i);
                if(v != null) values.add(new DBEntry<>(i, v));
            }
        }
        
        return values;
    }
    
    @Override
    public int size()
    {
        return getEntries().length;
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public void reset()
    {
        synchronized(dbBlockMap)
        {
            dbBlockMap.clear();
            idMap.clear();
            refCache = new DBEntry[0];
        }
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public DBEntry<T>[] getEntries()
    {
        synchronized(dbBlockMap)
        {
            if(refCache == null)
            {
                List<DBEntry<T>> tmp = new ArrayList<>();
    
                for(SortedMap<Integer,T> blockEntry : dbBlockMap.values())
                {
                    for(Entry<Integer,T> entry : blockEntry.entrySet())
                    {
                        tmp.add(new DBEntry<>(entry.getKey(), entry.getValue()));
                    }
                }
                
                refCache = tmp.toArray(new DBEntry[0]);
            }
            
            return refCache;
        }
    }
}
