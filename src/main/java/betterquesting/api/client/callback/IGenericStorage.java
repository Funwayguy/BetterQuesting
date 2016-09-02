package betterquesting.api.client.callback;

/**
 * Useful for passing information to and from GUIs or just attaching information to things
 */
public interface IGenericStorage<T>
{
	public int getStorageID();
	
	public T getStored();
	public void setStored(T obj);
}
