package betterquesting.quests.tasks.advanced;

import java.util.UUID;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

/**
 * Tasks implementing this can be interacted with through the Submit Station
 */
public interface IContainerTask
{
	public boolean canAcceptFluid(UUID owner, Fluid fluid);
	public boolean canAcceptItem(UUID owner, ItemStack item);
	
	/**
	 * Submits a FluidStack to this task and returns the leftovers if any
	 */
	public FluidStack submitFluid(UUID owner, FluidStack fluid);
	
	/**
	 * Submits an ItemStack from input to this task and puts any leftover containers/etc. in the output
	 */
	public void submitItem(UUID owner, Slot input, Slot output);
}
