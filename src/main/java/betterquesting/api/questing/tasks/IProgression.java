package betterquesting.api.questing.tasks;

import java.util.UUID;

// TODO: Deprecate after party progression split?
public interface IProgression<T> extends ITask
{
	void setUserProgress(UUID uuid, T data);
	
	T getUsersProgress(UUID... users);
	T getGlobalProgress();
	
	float getParticipation(UUID uuid);
}
