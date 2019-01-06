package betterquesting.api2.storage;

import java.util.*;

// Divides the database into smaller indexed blocks to speed up search times.
public abstract class BigDatabase<T> implements IDatabase<T>
{
    // Simple entries here represent the block index, NOT the index of the child database entries
    private final SortedSet<DBEntry<SortedSet<DBEntry<T>>>> dbBlocks = Collections.synchronizedSortedSet(new TreeSet<>((Comparator<DBEntry>)(o1, o2) -> o1.getValue() == o2.getValue() ? 0 : Integer.compare(o1.getID(), o2.getID())));
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
        synchronized(dbBlocks)
        {
            int blockID = 0;
            
            for(DBEntry<SortedSet<DBEntry<T>>> blockEntry : dbBlocks)
            {
                if(blockID != blockEntry.getID()) // There is a missing/unused block of IDs
                {
                    return blockID * blockSize;
                } else if(blockEntry.getValue().size() >= blockSize) // This block is full, continue
                {
                    blockID++;
                    continue;
                }
                
                DBEntry[] entryList = blockEntry.getValue().toArray(new DBEntry[0]);
                int startID = blockID * blockSize;
                
                for(int i = 0; i < entryList.length; i++)
                {
                    if(entryList[i].getID() != startID + i)
                    {
                        return startID + i;
                    }
                }
                
                return (blockEntry.getID() * blockSize) + entryList.length;
            }
            
            return blockID * blockSize;
        }
    }
    
    @Override
    public DBEntry<T> add(int id, T value)
    {
        if(value == null)
        {
            throw new NullPointerException("Value cannot be null");
        } else if(id < 0)
        {
            throw new IllegalArgumentException("ID cannot be negative");
        }
        
        int blockID = id / blockSize;
        
        synchronized(dbBlocks)
        {
            for(DBEntry<SortedSet<DBEntry<T>>> blockEntry : dbBlocks)
            {
                if(blockEntry.getID() > blockID)
                {
                    break;
                } else if(blockEntry.getID() == blockID)
                {
                    DBEntry<T> entry = new DBEntry<>(id, value);
                    
                    if(blockEntry.getValue().add(entry))
                    {
                        return entry;
                    } else
                    {
                        throw new IllegalArgumentException("ID or value is already contained within database");
                    }
                }
            }
            
            SortedSet<DBEntry<T>> block = Collections.synchronizedSortedSet(new TreeSet<>());
            DBEntry<T> entry = new DBEntry<>(id, value);
            block.add(entry);
            dbBlocks.add(new DBEntry<>(blockID, block));
            return entry;
        }
    }
    
    @Override
    public boolean removeID(int id)
    {
        synchronized(dbBlocks)
        {
            for(DBEntry<SortedSet<DBEntry<T>>> blockEntry : dbBlocks)
            {
                if(id / blockSize < blockEntry.getID())
                {
                    return false;
                } else if(removeIDFromBlock(blockEntry.getValue(), id))
                {
                    return true;
                }
            }
            
            return false;
        }
    }
    
    private boolean removeIDFromBlock(SortedSet<DBEntry<T>> block, int id)
    {
        if(id < 0)
        {
            return false;
        }
        
        Iterator<DBEntry<T>> iter = block.iterator();

        while(iter.hasNext())
        {
            DBEntry<T> entry = iter.next();
            
            if(entry.getID() == id)
            {
                iter.remove();
                return true;
            } else if(entry.getID() > id)
            {
                break;
            }
        }
        
        return false;
    }
    
    @Override
    public boolean removeValue(T value)
    {
        synchronized(dbBlocks)
        {
            for(DBEntry<SortedSet<DBEntry<T>>> blockEntry : dbBlocks)
            {
                if(removeValueFromBlock(blockEntry.getValue(), value))
                {
                    return true;
                }
            }
            
            return false;
        }
    }
    
    private boolean removeValueFromBlock(SortedSet<DBEntry<T>> block, T value)
    {
        if(value == null)
        {
            return false;
        }
        
        Iterator<DBEntry<T>> iter = block.iterator();

        while(iter.hasNext())
        {
            if(iter.next().getValue() == value)
            {
                iter.remove();
                return true;
            }
        }
        
        return false;
    }
    
    @Override
    public int getID(T value)
    {
        synchronized(dbBlocks)
        {
            for(DBEntry<SortedSet<DBEntry<T>>> blockEntry : dbBlocks)
            {
                int id = getIDFromBlock(blockEntry.getValue(), value);
                
                if(id >= 0)
                {
                    return id;
                }
            }
            
            return -1;
        }
    }
    
    private int getIDFromBlock(SortedSet<DBEntry<T>> block, T value)
    {
        if(value == null)
        {
            return -1;
        }
        
        for(DBEntry<T> entry : block)
        {
            if(entry.getValue() == value)
            {
                return entry.getID();
            }
        }
        
        return -1;
    }
    
    @Override
    public T getValue(int id)
    {
        synchronized(dbBlocks)
        {
            for(DBEntry<SortedSet<DBEntry<T>>> blockEntry : dbBlocks)
            {
                if(id / blockSize < blockEntry.getID())
                {
                    break;
                } else if(blockEntry.getID() != id / blockSize)
                {
                    continue;
                }
                
                return getValueFromBlock(blockEntry.getValue(), id);
            }
            
            return null;
        }
    }
    
    private T getValueFromBlock(SortedSet<DBEntry<T>> block, int id)
    {
        if(id < 0 || block.size() <= 0 || id > block.last().getID())
        {
            return null;
        }
        
        for(DBEntry<T> entry : block)
        {
            if(entry.getID() > id)
            {
                return null;
            } else if(entry.getID() == id)
            {
                return entry.getValue();
            }
        }
        
        return null;
    }
    
    /**
     * Unlike getValue(), this method can retrieve a whole set of entries in one pass instead of one for every ID
     * @param ids
     * @return List of database entries that match the provided IDs (may be rearranged during this action)
     */
    public List<DBEntry<T>> bulkLookup(int... ids)
    {
        if(ids == null || ids.length <= 0) return Collections.emptyList();
        
        Arrays.sort(ids);
        List<DBEntry<T>> values = new ArrayList<>();
        int index = 0;
        
        synchronized(dbBlocks)
        {
            for(DBEntry<SortedSet<DBEntry<T>>> blockEntry : dbBlocks)
            {
                Iterator<DBEntry<T>> blockSearch = null;
                DBEntry<T> entry = null;
                
                while(index < ids.length)
                {
                    int nxt = ids[index];
                    
                    if(nxt < 0 || nxt / blockSize < blockEntry.getID()) // Allow ID to catch up to block
                    {
                        index++;
                        continue;
                    } else if(nxt / blockSize > blockEntry.getID()) // Allow block to catch up to ID
                    {
                        break;
                    } else if(blockEntry.getValue().size() <= 0) // Empty block
                    {
                        break;
                    }
                    
                    if(blockSearch == null)
                    {
                        blockSearch = blockEntry.getValue().iterator();
                        entry = blockSearch.next();
                    }
                    
                    while(entry != null)
                    {
                        if(entry.getID() > nxt) break;
                        
                        if(entry.getID() == nxt)
                        {
                            values.add(entry);
                            entry = blockSearch.hasNext() ? blockSearch.next() : null;
                            break;
                        }
                        entry = blockSearch.hasNext() ? blockSearch.next() : null;
                    }
                    
                    index++; // Block search complete
                }
                
                if(index >= ids.length) break; // No more IDs to search
            }
        }
        
        return values;
    }
    
    @Override
    public int size()
    {
        int s = 0;
        
        synchronized(dbBlocks)
        {
            for(DBEntry<SortedSet<DBEntry<T>>> blockEntry : dbBlocks)
            {
                s += blockEntry.getValue().size();
            }
        }
        
        return s;
    }
    
    @Override
    public void reset()
    {
        dbBlocks.clear();
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public DBEntry<T>[] getEntries()
    {
        synchronized(dbBlocks)
        {
            DBEntry<T>[] array = new DBEntry[this.size()];
            int i = 0;
            
            for(DBEntry<SortedSet<DBEntry<T>>> blockEntry : dbBlocks)
            {
                for(DBEntry<T> entry : blockEntry.getValue().toArray(new DBEntry[0]))
                {
                    array[i] = entry;
                    i++;
                }
            }
            
            return array;
        }
    }
}
