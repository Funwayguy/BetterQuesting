package betterquesting.quests.tasks.party;

import java.util.UUID;

public interface IPartyTask<T>
{
	/**
	 * Returns the total progress from the given user's party.
	 * If no party is found it should return individual progress.
	 */
	public T GetPartyProgress(UUID uuid);
	
	/**
	 * Resets progress for all members of the given user's party.
	 */
	public void ResetPartyProgress(UUID uuid);
	
	/**
	 * Sets the completion state for all members of the given user's party
	 */
	public void SetPartyCompletion(UUID uuid, boolean state);
}
