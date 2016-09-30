package betterquesting.api.events;

import cpw.mods.fml.common.eventhandler.Event;

public abstract class DatabaseEvent extends Event
{
	public static class DatabaseUpdated extends DatabaseEvent
	{
		public DatabaseUpdated()
		{
		}
	}
}
