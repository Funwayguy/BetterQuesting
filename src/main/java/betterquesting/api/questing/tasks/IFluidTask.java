package betterquesting.api.questing.tasks;

import net.minecraftforge.fluids.FluidStack;

import java.util.UUID;

public interface IFluidTask extends ITask
{
    // TODO: Add IQuest parameter to these calls
	boolean canAcceptFluid(UUID owner, FluidStack fluid);
	FluidStack submitFluid(UUID owner, FluidStack fluid);
}
