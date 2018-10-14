package betterquesting.api2.storage;

import java.util.*;

// It's up to child classes to define how T is parsed as K
public abstract class SimpleDatabase<T> implements IDatabase<T>
{
    private final SortedSet<DBEntry<T>> listDB = Collections.synchronizedSortedSet(new TreeSet<>((Comparator<DBEntry>)(o1, o2) -> o1.getValue() == o2.getValue() ? 0 : Integer.compare(o1.getID(), o2.getID())));
    
    @Override
    public int nextID()
    {
        if(listDB.size() <= 0 || listDB.last().getID() == listDB.size() - 1)
        {
            return listDB.size();
        }
        
        synchronized(listDB)
        {
            int i = 0;
            
            Iterator<DBEntry<T>> iterator = listDB.iterator();
            
            while(iterator.hasNext() && iterator.next().getID() == i)
            {
                i++;
            }
            
            return i;
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
        } else
        {
            DBEntry<T> entry = new DBEntry<>(id, value);
            
            if(listDB.add(entry))
            {
                return entry;
            } else
            {
                throw new IllegalArgumentException("ID or value is already contained within database");
            }
        }
    }
    
    @Override
    public boolean removeID(int key)
    {
        if(key < 0)
        {
            return false;
        }
        
        synchronized(listDB)
        {
            Iterator<DBEntry<T>> iter = listDB.iterator();
    
            while(iter.hasNext())
            {
                DBEntry<T> entry = iter.next();
                
                if(entry.getID() == key)
                {
                    iter.remove();
                    return true;
                } else if(entry.getID() > key)
                {
                    break;
                }
            }
        }
        
        return false;
    }
    
    @Override
    public boolean removeValue(T value)
    {
        if(value == null)
        {
            return false;
        }
        
        synchronized(listDB)
        {
            Iterator<DBEntry<T>> iter = listDB.iterator();
    
            while(iter.hasNext())
            {
                if(iter.next().getValue() == value)
                {
                    iter.remove();
                    return true;
                }
            }
        }
        
        return false;
    }
    
    @Override
    public int getID(T value)
    {
        if(value == null)
        {
            return -1;
        }
        
        synchronized(listDB)
        {
            for(DBEntry<T> entry : listDB)
            {
                if(entry.getValue() == value)
                {
                    return entry.getID();
                }
            }
        }
        
        return -1;
    }
    
    @Override
    public T getValue(int id)
    {
        if(id < 0 || listDB.size() <= 0 || id > listDB.last().getID())
        {
            return null;
        }
        
        synchronized(listDB)
        {
            for(DBEntry<T> entry : listDB)
            {
                if(entry.getID() > id)
                {
                    return null;
                } else if(entry.getID() == id)
                {
                    return entry.getValue();
                }
            }
        }
        
        return null;
    }
    
    @Override
    public int size()
    {
        return listDB.size();
    }
    
    @Override
    public void reset()
    {
        listDB.clear();
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public DBEntry<T>[] getEntries()
    {
        return listDB.toArray(new DBEntry[0]);
    }
}