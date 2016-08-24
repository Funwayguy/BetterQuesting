package betterquesting.api.database;

import java.util.List;

/**
 * Object storage with integer keys. Can be mapped to a HashMap for general use or something more complex
 */
public interface IRegStorage<T>
{
	public int nextID();
	
	public boolean add(T obj, int id);
	public boolean remove(int id);
	public boolean remove(T obj);
	
	public T getValue(int id);
	public int getKey(T obj);
	
	public int size();
	public void reset();
	
	public List<T> getAllValues();
	public List<Integer> getAllKeys();
}
