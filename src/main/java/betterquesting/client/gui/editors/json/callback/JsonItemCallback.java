package betterquesting.client.gui.editors.json.callback;

import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import betterquesting.api.misc.ICallback;
import betterquesting.api.utils.BigItemStack;
import betterquesting.api.utils.JsonHelper;

public class JsonItemCallback implements ICallback<BigItemStack>
{
	private BigItemStack baseStack = new BigItemStack(Blocks.STONE);
	private final NBTTagCompound json;
	
	public JsonItemCallback(NBTTagCompound json)
	{
		this(json, new BigItemStack(Blocks.STONE));
	}
	
	public JsonItemCallback(NBTTagCompound json, BigItemStack stack)
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
		
		JsonHelper.ClearCompoundTag(json);
		JsonHelper.ItemStackToJson(baseStack, json);
	}
	
	public NBTTagCompound getJsonObject()
	{
		return json;
	}
	
	public BigItemStack getItemStack()
	{
		return baseStack;
	}
}
