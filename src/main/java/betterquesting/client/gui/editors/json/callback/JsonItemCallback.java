package betterquesting.client.gui.editors.json.callback;

import net.minecraft.init.Blocks;
import betterquesting.api.misc.ICallback;
import betterquesting.api.utils.BigItemStack;
import betterquesting.api.utils.JsonHelper;
import com.google.gson.JsonObject;

public class JsonItemCallback implements ICallback<BigItemStack>
{
	private BigItemStack baseStack = new BigItemStack(Blocks.STONE);
	private final JsonObject json;
	
	public JsonItemCallback(JsonObject json)
	{
		this(json, new BigItemStack(Blocks.STONE));
	}
	
	public JsonItemCallback(JsonObject json, BigItemStack stack)
	{
		this.json = json;
		this.baseStack = stack;
	}
	
	public void setValue(BigItemStack stack)
	{
		if(stack != null)
		{
			this.baseStack = stack;
		} else
		{
			this.baseStack = new BigItemStack(Blocks.STONE);
		}
		
		json.entrySet().clear();
		JsonHelper.ItemStackToJson(baseStack, json);
	}
	
	public JsonObject getJsonObject()
	{
		return json;
	}
	
	public BigItemStack getItemStack()
	{
		return baseStack;
	}
}
