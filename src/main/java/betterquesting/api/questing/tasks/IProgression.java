package betterquesting.api.questing.tasks;

import java.util.UUID;

public interface IProgression<T>
{
	public void setUserProgress(UUID uuid, T data);
	
	public T getUsersProgress(UUID... users);
	public T getGlobalProgress();
	
	public float getParticipation(UUID uuid);
}
