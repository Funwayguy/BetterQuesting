package adv_director.rw2.api.client.gui.controls;

public class PanelButtonStorage<T> extends PanelButton
{
	private T stored = null;
	
	public PanelButtonStorage(int id, String txt, T value)
	{
		super(id, txt);
		this.setStoredValue(value);
	}
	
	public PanelButtonStorage<T> setStoredValue(T value)
	{
		this.stored = value;
		return this;
	}
	
	public T getStoredValue()
	{
		return stored;
	}
}
