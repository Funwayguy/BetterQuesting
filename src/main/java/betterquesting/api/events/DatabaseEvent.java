package betterquesting.api.events;

import net.minecraftforge.fml.common.eventhandler.Event;

/**
 * Fired when the whole questing database for world is modified, loaded or saved.
 * Can be used to save/load custom databases in expansions or update dependent GUIs
 */
public abstract class DatabaseEvent extends Event
{
	public static class Update extends DatabaseEvent
	{
	}
	
	public static class Load extends DatabaseEvent
	{
	}
	
	public static class Save extends DatabaseEvent
	{
	}
}
