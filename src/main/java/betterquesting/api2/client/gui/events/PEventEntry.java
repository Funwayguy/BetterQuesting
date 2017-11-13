package betterquesting.api2.client.gui.events;

import java.util.ArrayList;

public class PEventEntry<T extends PanelEvent>
{
	private final ArrayList<IPEventListener> listeners = new ArrayList<IPEventListener>();
	private final PEventFilter<T> filter;
	
	public PEventEntry(Class<T> type)
	{
		this.filter = new PEventFilter<T>(type);
	}
	
	public void registerListener(IPEventListener l)
	{
		if(l == null || listeners.contains(l))
		{
			return;
		}
		
		listeners.add(l);
	}
	
	public void unregisterListener(IPEventListener l)
	{
		listeners.remove(l);
	}
	
	public PEventFilter<T> getTypeFilter()
	{
		return this.filter;
	}
	
	public void fire(PanelEvent event)
	{
		if(!filter.isCompatible(event))
		{
			return;
		}
		
		for(IPEventListener l : listeners)
		{
			l.onPanelEvent(event);
		}
	}
}
