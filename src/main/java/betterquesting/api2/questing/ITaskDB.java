package betterquesting.api2.questing;

import betterquesting.api.questing.tasks.ITask;
import betterquesting.api2.storage.IDatabase;
import betterquesting.api2.storage.INBTProgress;
import betterquesting.api2.storage.INBTSaveLoad;
import net.minecraft.nbt.NBTTagList;

public interface ITaskDB extends IDatabase<ITask>, INBTSaveLoad<NBTTagList>, INBTProgress<NBTTagList>
{
}
