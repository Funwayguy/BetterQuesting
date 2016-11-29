package betterquesting.api.placeholders;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.Fluid;
import betterquesting.core.BetterQuesting;

public class FluidPlaceholder extends Fluid
{
	public static Fluid fluidPlaceholder = new FluidPlaceholder();
	
	public FluidPlaceholder()
	{
		super("betterquesting.placeholder", new ResourceLocation(BetterQuesting.MODID + ":blocks/fluid_placeholder"), new ResourceLocation(BetterQuesting.MODID + ":blocks/fluid_placeholder"));
	}
}
