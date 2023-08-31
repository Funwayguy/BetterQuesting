package betterquesting.api.questing.tasks;

import betterquesting.api.questing.IQuest;
import betterquesting.api2.storage.DBEntry;

import java.util.UUID;

public interface IEnergyTask extends ITask {
  int submitEnergy(UUID owner, DBEntry<IQuest> quest, int energy, boolean doSubmit);
}
