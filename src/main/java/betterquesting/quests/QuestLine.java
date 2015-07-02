package betterquesting.quests;

import java.util.ArrayList;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

public class QuestLine
{
	public String name = "New Quest Line";
	public boolean mainQuest = false;
	public ArrayList<Integer> questList = new ArrayList<Integer>();
	
	public void writeToJSON(JsonObject json)
	{
		json.addProperty("name", name);
		
		JsonArray jArr = new JsonArray();
		
		for(int id : questList)
		{
			jArr.add(new JsonPrimitive(id));
		}
		
		json.add("quests", jArr);
	}
	
	public void readFromJSON(JsonObject json)
	{
		name = json.get("name").getAsString();
		
		questList.clear();
		JsonArray jArr = json.getAsJsonArray("quests");
		
		for(JsonElement element : jArr)
		{
			questList.add(element.getAsInt());
		}
	}
}
