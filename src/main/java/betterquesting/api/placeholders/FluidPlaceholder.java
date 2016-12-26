package betterquesting.api.placeholders;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.Fluid;

public class FluidPlaceholder extends Fluid
{
	public static Fluid fluidPlaceholder = new FluidPlaceholder();
	
	public FluidPlaceholder()
	{
		super("betterquesting.placeholder", new ResourceLocation("betterquesting:blocks/fluid_placeholder"), new ResourceLocation("betterquesting:blocks/fluid_placeholder"));
	}
}
