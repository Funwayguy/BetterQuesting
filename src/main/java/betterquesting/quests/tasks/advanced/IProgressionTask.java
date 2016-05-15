package betterquesting.quests.tasks.advanced;

import java.util.UUID;

public interface IProgressionTask<T>
{
	/**
	 * Sets the given users progress
	 */
	public void SetUserProgress(UUID uuid, T progress);
	
	/**
	 * Returns the progress for the given user
	 */
	public T GetUserProgress(UUID uuid);
	
	/**
	 * Returns the total progress from the given user's party.
	 * If no party is found it should return individual progress.
	 */
	public T GetPartyProgress(UUID uuid);
	
	/**
	 * Returns the global progress from all players.
	 * Should be within global quests
	 */
	public T GetGlobalProgress();
}
