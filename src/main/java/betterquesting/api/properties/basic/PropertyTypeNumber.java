package betterquesting.api.properties.basic;

import net.minecraft.util.ResourceLocation;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

public class PropertyTypeNumber extends PropertyTypeBase<Number>
{
	public PropertyTypeNumber(ResourceLocation key, Number def)
	{
		super(key, def);
	}
	
	@Override
	public Number readValue(JsonElement json)
	{
		if(json == null || !json.isJsonPrimitive())
		{
			return this.getDefault();
		}
		
		try
		{
			return json.getAsNumber();
		} catch(Exception e)
		{
			return this.getDefault();
		}
	}
	
	@Override
	public JsonElement writeValue(Number value)
	{
		if(value == null)
		{
			return new JsonPrimitive(this.getDefault());
		}
		
		return new JsonPrimitive(value);
	}
}
