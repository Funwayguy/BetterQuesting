package adv_director.rw2.api.client.gui.controls;

import adv_director.rw2.api.client.gui.misc.IGuiRect;

public class PanelButtonStorage<T> extends PanelButton
{
	private T stored = null;
	
	public PanelButtonStorage(IGuiRect rect, int id, String txt, T value)
	{
		super(rect, id, txt);
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
