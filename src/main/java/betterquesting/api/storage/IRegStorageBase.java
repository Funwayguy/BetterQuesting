package betterquesting.api.storage;

import java.util.List;

// Can be mapped to a hash map or something more complicated
public interface IRegStorageBase<K,V>
{
	public K nextKey();
	
	public boolean add(V value, K key);
	public boolean removeKey(K key);
	public boolean removeValue(V value);
	
	public V getValue(K key);
	public K getKey(V value);
	
	public int size();
	public void reset();
	
	public List<V> getAllValues();
	public List<K> getAllKeys();
}
