package betterquesting.questing.tasks;

import betterquesting.api.questing.IQuest;
import betterquesting.api.questing.tasks.ITask;
import betterquesting.api2.storage.DBEntry;
import betterquesting.api2.utils.ParticipantInfo;

import javax.annotation.Nonnull;

public interface ITaskTickable extends ITask
{
    void tickTask(@Nonnull ParticipantInfo pInfo, DBEntry<IQuest> quest);
}
