package betterquesting.quests;

import java.util.Map.Entry;
import betterquesting.api.enums.EnumSaveType;
import betterquesting.api.quests.properties.IQuestInfo;
import betterquesting.api.quests.properties.IQuestProperty;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class QuestInfo implements IQuestInfo
{
	private JsonObject jInfo = new JsonObject();
	
	@Override
	public <T> T getProperty(IQuestProperty<T> prop)
	{
		if(prop == null)
		{
			return null;
		}
		
		JsonElement jProp = jInfo.get(prop.getKey().toString());
		
		if(jProp != null)
		{
			prop.getDefault();
		}
		
		return prop.readValue(jProp);
	}
	
	@Override
	public JsonObject writeToJson(JsonObject json, EnumSaveType saveType)
	{
		for(Entry<String,JsonElement> entry : jInfo.entrySet())
		{
			json.add(entry.getKey(), entry.getValue());
		}
		
		return json;
	}
	
	@Override
	public void readFromJson(JsonObject json, EnumSaveType saveType)
	{
		jInfo = new JsonObject();
		
		for(Entry<String,JsonElement> entry : json.entrySet())
		{
			jInfo.add(entry.getKey(), entry.getValue());
		}
	}
}
