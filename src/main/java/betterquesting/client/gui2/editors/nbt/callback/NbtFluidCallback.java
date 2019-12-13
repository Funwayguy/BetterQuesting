package betterquesting.client.gui2.editors.nbt.callback;

import betterquesting.api.misc.ICallback;
import betterquesting.api.utils.JsonHelper;
import net.minecraft.fluid.Fluids;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.fluids.FluidStack;

public class NbtFluidCallback implements ICallback<FluidStack>
{
	private final CompoundNBT nbt;
	
	public NbtFluidCallback(CompoundNBT nbt)
	{
		this.nbt = nbt;
	}
	
	public void setValue(FluidStack stack)
	{
		FluidStack baseStack;
		
		if(stack != null)
		{
			baseStack = stack;
		} else
		{
			baseStack = new FluidStack(Fluids.WATER, 1000);
		}
		
		JsonHelper.ClearCompoundTag(nbt);
		JsonHelper.FluidStackToJson(baseStack, nbt);
	}
}
