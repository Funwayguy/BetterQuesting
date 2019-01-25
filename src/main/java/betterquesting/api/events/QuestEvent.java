package betterquesting.api.events;

import net.minecraftforge.fml.common.eventhandler.Event;

import java.util.UUID;

public abstract class QuestEvent extends Event
{
	private final UUID user;
	private final int questId;
	
	public int getQuestID()
	{
		return questId;
	}
	
	public UUID getUser()
	{
		return user;
	}
	
	public QuestEvent(int questId, UUID user)
	{
		this.user = user;
		this.questId = questId;
	}
	
	public static class QuestComplete extends QuestEvent
	{
		public QuestComplete(int questId, UUID user)
		{
			super(questId, user);
		}
	}
}
