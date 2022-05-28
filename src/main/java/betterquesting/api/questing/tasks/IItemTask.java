package betterquesting.api.questing.tasks;

import betterquesting.api.questing.IQuest;
import betterquesting.api2.storage.DBEntry;
import betterquesting.api2.utils.ParticipantInfo;
import net.minecraft.item.ItemStack;

import java.util.UUID;

public interface IItemTask extends ITask
{
	boolean canAcceptItem(UUID owner, DBEntry<IQuest> quest, ItemStack stack);
	ItemStack submitItem(UUID owner, DBEntry<IQuest> quest, ItemStack stack);

    /**
     * @param items read-only list of items
     */
    default void retrieveItems(ParticipantInfo pInfo, DBEntry<IQuest> quest, ItemStack[] items) {}
}
