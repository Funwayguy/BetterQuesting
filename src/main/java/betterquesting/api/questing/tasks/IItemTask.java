package betterquesting.api.questing.tasks;

import java.util.UUID;
import net.minecraft.item.ItemStack;

public interface IItemTask
{
	public boolean canAcceptItem(UUID owner, ItemStack stack);
	public ItemStack submitItem(UUID owner, ItemStack stack);
}
