package betterquesting.api.questing.tasks;

import betterquesting.api.questing.IQuest;
import betterquesting.api2.storage.DBEntry;
import net.minecraft.item.ItemStack;

import java.util.UUID;

public interface IItemTask extends ITask {
    boolean canAcceptItem(UUID owner, DBEntry<IQuest> quest, ItemStack stack);

    ItemStack submitItem(UUID owner, DBEntry<IQuest> quest, ItemStack stack);
}
