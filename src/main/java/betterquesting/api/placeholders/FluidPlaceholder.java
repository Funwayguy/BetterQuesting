package betterquesting.api.placeholders;

import net.minecraftforge.fluids.Fluid;

public class FluidPlaceholder extends Fluid {
    public static Fluid fluidPlaceholder = new FluidPlaceholder();

    public FluidPlaceholder() {
        super("betterquesting.placeholder");
    }
}
