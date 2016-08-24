package betterquesting.api.events;

import cpw.mods.fml.common.eventhandler.Event;

public abstract class QuestDataEvent extends Event
{
	public static class DatabaseUpdated extends QuestDataEvent
	{
		public DatabaseUpdated()
		{
		}
	}
}
