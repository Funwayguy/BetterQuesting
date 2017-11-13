package betterquesting.api2.client.gui.events;

import java.util.HashMap;
import java.util.Map.Entry;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class PEventBroadcaster
{
	public static PEventBroadcaster INSTANCE = new PEventBroadcaster();
	
	private final HashMap<Class<? extends PanelEvent>, PEventEntry<? extends PanelEvent>> entryList = new HashMap<Class<? extends PanelEvent>, PEventEntry<? extends PanelEvent>>();
	
	/**
	 * Registers an event listener for multiple given event types
	 */
	public void register(IPEventListener l, Class<? extends PanelEvent>[] types)
	{
		if(l == null || types == null)
		{
			return;
		}
		
		for(Class<? extends PanelEvent> t : types)
		{
			register(l, t);
		}
	}
	
	/**
	 * Registers an event listener for the given event type
	 */
	public <T extends PanelEvent> void register(IPEventListener l, Class<T> type)
	{
		if(l == null || type == null)
		{
			return;
		}
		
		PEventEntry<?> pe = entryList.get(type);
		
		if(pe == null)
		{
			pe = new PEventEntry<T>(type);
			entryList.put(type, pe);
		}
		
		pe.registerListener(l);
	}
	
	public void unregister(IPEventListener l)
	{
		for(Entry<Class<? extends PanelEvent>, PEventEntry<? extends PanelEvent>> e : entryList.entrySet())
		{
			e.getValue().unregisterListener(l);
		}
	}
	
	/**
	 * Fires a panel event to all relevant listeners and returns true if cancelled
	 */
	public boolean postEvent(PanelEvent event)
	{
		for(Entry<Class<? extends PanelEvent>, PEventEntry<? extends PanelEvent>> e : entryList.entrySet())
		{
			e.getValue().fire(event);
		}
		
		return event.canCancel()? event.isCancelled() : false;
	}
	
	/**
	 * Clears event listeners whenever a new GUI loads. If you must have cross GUI communication either handle this yourself or re-register the relevant listeners.
	 */
	@SubscribeEvent
	public void onGuiOpened(GuiOpenEvent event)
	{
		entryList.clear();
	}
}
