package betterquesting.api.questing.tasks;

import net.minecraft.item.ItemStack;

import java.util.UUID;

public interface IItemTask extends ITask
{
	boolean canAcceptItem(UUID owner, ItemStack stack);
	ItemStack submitItem(UUID owner, ItemStack stack);
}
