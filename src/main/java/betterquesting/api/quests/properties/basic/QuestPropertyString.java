package betterquesting.api.quests.properties.basic;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import net.minecraft.util.ResourceLocation;

public class QuestPropertyString extends QuestPropertyBase<String>
{
	public QuestPropertyString(ResourceLocation key, String def)
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
