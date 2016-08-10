package betterquesting.quests;

import java.util.ArrayList;
import java.util.List;
import betterquesting.api.quests.IQuestLineContainer;
import betterquesting.api.quests.IQuestLineEntry;
import betterquesting.api.utils.JsonHelper;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class QuestLine implements IQuestLineContainer
{
	private String name = "questline.untitled.name";
	private String description = "questline.untitled.desc";
	private final ArrayList<IQuestLineEntry> questList = new ArrayList<IQuestLineEntry>();
	
	@Override
	public String getUnlocalisedName()
	{
		return name;
	}
	
	@Override
	public String getUnlocalisedDescription()
	{
		return description;
	}
	
	@Override
	public IQuestLineEntry getQuestEntry(int questId)
	{
		for(IQuestLineEntry entry : questList)
		{
			if(entry != null && entry.getQuestID() == questId)
			{
				return entry;
			}
		}
		
		return null;
	}
	
	@Override
	public List<IQuestLineEntry> getAllQuests()
	{
		return questList;
	}
	
	@Override
	public JsonObject writeToJson(JsonObject json)
	{
		json.addProperty("name", name);
		json.addProperty("description", description);
		
		JsonArray jArr = new JsonArray();
		
		for(IQuestLineEntry entry : questList)
		{
			jArr.add(entry.writeToJson(new JsonObject()));
		}
		
		json.add("quests", jArr);
		return json;
	}
	
	@Override
	public void readFromJson(JsonObject json)
	{
		name = JsonHelper.GetString(json, "name", "New Quest Line");
		description = JsonHelper.GetString(json, "description", "No description");
		
		//boolean remap = false;
		
		questList.clear();
		for(JsonElement entry : JsonHelper.GetArray(json, "quests"))
		{
			if(entry == null)
			{
				continue;
			}
			
			if(entry.isJsonPrimitive() && entry.getAsJsonPrimitive().isNumber()) // Backwards compatibility
			{
				questList.add(new QuestLineEntry(entry.getAsInt(), 0, 0));
			} else if(entry.isJsonObject())
			{
				questList.add(new QuestLineEntry(entry.getAsJsonObject()));
			}
		}
	}
}
