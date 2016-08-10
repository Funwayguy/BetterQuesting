package betterquesting.api.events;

import java.util.UUID;
import cpw.mods.fml.common.eventhandler.Event;

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
	
	public static class TaskComplete extends QuestEvent
	{
		public TaskComplete(int questId, UUID user)
		{
			super(questId, user);
		}
	}
	
	public static class RewardClaim extends QuestEvent
	{
		public RewardClaim(int questId, UUID user)
		{
			super(questId, user);
		}
	}
}
