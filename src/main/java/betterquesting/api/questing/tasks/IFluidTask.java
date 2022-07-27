package betterquesting.api.questing.tasks;

import betterquesting.api.questing.IQuest;
import betterquesting.api2.storage.DBEntry;
import betterquesting.api2.utils.ParticipantInfo;
import java.util.UUID;
import net.minecraftforge.fluids.FluidStack;

public interface IFluidTask extends ITask {
    boolean canAcceptFluid(UUID owner, DBEntry<IQuest> quest, FluidStack fluid);

    FluidStack submitFluid(UUID owner, DBEntry<IQuest> quest, FluidStack fluid);

    /**
     * @param fluids read-only list of fluids
     */
    default void retrieveFluids(ParticipantInfo pInfo, DBEntry<IQuest> quest, FluidStack[] fluids) {}
}
