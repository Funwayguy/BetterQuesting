package adv_director.api.placeholders;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.Fluid;

public class FluidPlaceholder extends Fluid
{
	public static Fluid fluidPlaceholder = new FluidPlaceholder();
	
	public FluidPlaceholder()
	{
		super("adv_director.placeholder", new ResourceLocation("adv_director:blocks/fluid_placeholder"), new ResourceLocation("adv_director:blocks/fluid_placeholder"));
	}
}
