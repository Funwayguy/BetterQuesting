package betterquesting.api.properties.basic;

import net.minecraft.util.ResourceLocation;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

public class PropertyTypeString extends PropertyTypeBase<String>
{
	public PropertyTypeString(ResourceLocation key, String def)
	{
		super(key, def);
	}
	
	@Override
	public String readValue(JsonElement json)
	{
		if(json == null || !json.isJsonPrimitive())
		{
			return this.getDefault();
		}
		
		return json.getAsString();
	}
	
	@Override
	public JsonElement writeValue(String value)
	{
		if(value == null)
		{
			return new JsonPrimitive(this.getDefault());
		}
		
		return new JsonPrimitive(value);
	}
}
