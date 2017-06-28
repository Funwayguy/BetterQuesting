package adv_director.client.gui.editors.json.callback;

import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import adv_director.api.misc.ICallback;
import adv_director.api.utils.JsonHelper;
import com.google.gson.JsonObject;

public class JsonFluidCallback implements ICallback<FluidStack>
{
	private FluidStack baseStack = new FluidStack(FluidRegistry.WATER, 1000);
	private final JsonObject json;
	
	public JsonFluidCallback(JsonObject json)
	{
		this(json, new FluidStack(FluidRegistry.WATER, 1000));
	}
	
	public JsonFluidCallback(JsonObject json, FluidStack stack)
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
		
		json.entrySet().clear();
		JsonHelper.FluidStackToJson(baseStack, json);
	}
	
	public JsonObject getJsonObject()
	{
		return json;
	}
	
	public FluidStack getFluidStack()
	{
		return baseStack;
	}
}
