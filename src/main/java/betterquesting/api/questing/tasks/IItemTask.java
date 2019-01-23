package betterquesting.api.questing.tasks;

import betterquesting.api.questing.IQuest;
import net.minecraft.item.ItemStack;

import java.util.UUID;

public interface IItemTask extends ITask
{
	boolean canAcceptItem(UUID owner, IQuest quest, ItemStack stack);
	ItemStack submitItem(UUID owner, IQuest quest, ItemStack stack);
}
