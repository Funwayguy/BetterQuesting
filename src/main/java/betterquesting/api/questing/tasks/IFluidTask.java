package betterquesting.api.questing.tasks;

import java.util.UUID;
import net.minecraftforge.fluids.FluidStack;

public interface IFluidTask
{
	public boolean canAcceptFluid(UUID owner, FluidStack fluid);
	public FluidStack submitFluid(UUID owner, FluidStack fluid);
}
