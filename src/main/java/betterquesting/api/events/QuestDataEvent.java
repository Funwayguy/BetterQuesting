package betterquesting.api.events;

import cpw.mods.fml.common.eventhandler.Event;

public abstract class QuestDataEvent extends Event
{
	public static class DatabaseUpdateEvent extends QuestDataEvent
	{
		private final EventDatabase database;
		
		public DatabaseUpdateEvent(EventDatabase database)
		{
			this.database = database;
		}
		
		public EventDatabase getDatabase()
		{
			return this.database;
		}
	}
	
	public static enum EventDatabase
	{
		QUEST_MAIN,
		QUEST_PROP,
		QUEST_LINE,
		PARTY,
		LIVES;
	}
}
