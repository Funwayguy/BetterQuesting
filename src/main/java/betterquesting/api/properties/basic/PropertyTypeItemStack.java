package betterquesting.api.properties.basic;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.util.ResourceLocation;
import betterquesting.api.utils.BigItemStack;
import betterquesting.api.utils.JsonHelper;

public class PropertyTypeItemStack extends PropertyTypeBase<BigItemStack>
{
	public PropertyTypeItemStack(ResourceLocation key, BigItemStack def)
	{
		super(key, def);
	}
	
	@Override
	public BigItemStack readValue(JsonElement json)
	{
		if(json == null || !json.isJsonObject())
		{
			return this.getDefault();
		}
		
		return JsonHelper.JsonToItemStack(json.getAsJsonObject());
	}
	
	@Override
	public JsonElement writeValue(BigItemStack value)
	{
		JsonObject json = new JsonObject();
		
		if(value == null)
		{
			JsonHelper.ItemStackToJson(getDefault(), json);
		} else
		{
			JsonHelper.ItemStackToJson(value, json);
		}
		
		return json;
	}
}
