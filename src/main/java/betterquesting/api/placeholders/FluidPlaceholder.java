package betterquesting.api.placeholders;

import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.IFluidState;
import net.minecraft.fluid.WaterFluid;

public class FluidPlaceholder extends WaterFluid
{
	public static Fluid fluidPlaceholder = new FluidPlaceholder();
	
	public FluidPlaceholder()
	{
	    super();
		//super("betterquesting.placeholder", new ResourceLocation("betterquesting:blocks/fluid_placeholder"), new ResourceLocation("betterquesting:blocks/fluid_placeholder"));
	}
    
    @Override
    public boolean isSource(IFluidState state)
    {
        return false;
    }
    
    @Override
    public int getLevel(IFluidState p_207192_1_)
    {
        return 0;
    }
}
