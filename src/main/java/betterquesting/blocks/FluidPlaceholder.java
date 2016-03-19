package betterquesting.blocks;

import betterquesting.core.BetterQuesting;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.Fluid;

public class FluidPlaceholder extends Fluid
{
	public FluidPlaceholder()
	{
		super("betterquesting.placeholder", new ResourceLocation(BetterQuesting.MODID + ":blocks/fluid_placeholder"), new ResourceLocation(BetterQuesting.MODID + ":blocks/fluid_placeholder"));
	}
}
