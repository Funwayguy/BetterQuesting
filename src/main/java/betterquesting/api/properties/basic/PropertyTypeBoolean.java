package betterquesting.api.properties.basic;

import net.minecraft.util.ResourceLocation;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

public class PropertyTypeBoolean extends PropertyTypeBase<Boolean>
{
	public PropertyTypeBoolean(ResourceLocation key, Boolean def)
	{
		super(key, def);
	}
	
	@Override
	public Boolean readValue(JsonElement json)
	{
		if(json == null || !json.isJsonPrimitive())
		{
			return this.getDefault();
		}
		
		try
		{
			return json.getAsBoolean();
		} catch(Exception e)
		{
			return this.getDefault();
		}
	}
	
	@Override
	public JsonElement writeValue(Boolean value)
	{
		if(value == null)
		{
			return new JsonPrimitive(this.getDefault());
		}
		
		return new JsonPrimitive(value);
	}
}
