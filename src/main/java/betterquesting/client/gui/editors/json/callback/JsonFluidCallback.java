package betterquesting.client.gui.editors.json.callback;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import betterquesting.api.misc.ICallback;
import betterquesting.api.utils.JsonHelper;

public class JsonFluidCallback implements ICallback<FluidStack>
{
	private FluidStack baseStack = new FluidStack(FluidRegistry.WATER, 1000);
	private final NBTTagCompound json;
	
	public JsonFluidCallback(NBTTagCompound json)
	{
		this(json, new FluidStack(FluidRegistry.WATER, 1000));
	}
	
	public JsonFluidCallback(NBTTagCompound json, FluidStack stack)
	{
		this.json = json;
		this.baseStack = stack;
	}
	
	public void setValue(FluidStack stack)
	{
		if(stack != null)
		{
			this.baseStack = stack;
		} else
		{
			this.baseStack = new FluidStack(FluidRegistry.WATER, 1000);
		}
		
		JsonHelper.ClearCompoundTag(json);
		JsonHelper.FluidStackToJson(baseStack, json);
	}
	
	public NBTTagCompound getJsonObject()
	{
		return json;
	}
	
	public FluidStack getFluidStack()
	{
		return baseStack;
	}
}
