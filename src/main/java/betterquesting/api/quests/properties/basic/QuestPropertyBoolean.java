package betterquesting.api.quests.properties.basic;

import net.minecraft.util.ResourceLocation;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

public class QuestPropertyBoolean extends QuestPropertyBase<Boolean>
{
	public QuestPropertyBoolean(ResourceLocation key, Boolean def)
	{
		super(key, def);
	}
	
	@Override
	public Boolean readValue(JsonElement json)
	{
		if(json == null || !json.isJsonPrimitive() || !json.getAsJsonPrimitive().isBoolean())
		{
			this.getDefault();
		}
		
		return json.getAsBoolean();
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
