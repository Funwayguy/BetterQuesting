package betterquesting.quests;

import java.util.ArrayList;
import java.util.HashMap;
import net.minecraft.entity.player.EntityPlayer;
import betterquesting.utils.JsonHelper;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class QuestDatabase
{
	static HashMap<Integer, QuestInstance> questDB = new HashMap<Integer, QuestInstance>();
	public static ArrayList<QuestLine> questLines = new ArrayList<QuestLine>();
	
	/**
	 * @return the next free ID within the quest database
	 */
	public static int getUniqueID()
	{
		int id = 0;
		
		while(questDB.containsKey(id))
		{
			id += 1;
		}
		
		return id;
	}
	
	public static void UpdateAll(EntityPlayer player)
	{
		for(QuestInstance quest : questDB.values())
		{
			quest.Update(player);
		}
	}
	
	public static QuestInstance getQuest(int id)
	{
		return questDB.get(id);
	}
	
	public static void writeToJSON(JsonObject json)
	{
		JsonArray dbJson = new JsonArray();
		for(QuestInstance quest : questDB.values())
		{
			JsonObject questJson = new JsonObject();
			quest.writeToJSON(questJson);
			dbJson.add(questJson);
		}
		json.add("questDatabase", dbJson);
		
		JsonArray jArray = new JsonArray();
		for(QuestLine line : questLines)
		{
			JsonObject tmp = new JsonObject();
			line.writeToJSON(tmp);
			jArray.add(tmp);
		}
		json.add("questLines", jArray);
	}
	
	public static void readFromJSON(JsonObject json)
	{
		questDB.clear();
		for(JsonElement entry : JsonHelper.GetArray(json, "questDatabase"))
		{
			if(entry == null || !entry.isJsonObject())
			{
				continue;
			}
			
			QuestInstance quest = new QuestInstance(-1, false);
			quest.readFromJSON(entry.getAsJsonObject());
			
			if(quest.questID >= 0)
			{
				questDB.put(quest.questID, quest);
			}
		}
		
		questLines.clear();
		for(JsonElement entry : JsonHelper.GetArray(json, "questLines"))
		{
			if(entry == null || !entry.isJsonObject())
			{
				continue;
			}
			
			QuestLine line = new QuestLine();
			line.readFromJSON(entry.getAsJsonObject());
			questLines.add(line);
		}
	}
}
