package betterquesting.quests;

import java.util.HashMap;
import net.minecraft.entity.player.EntityPlayer;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class QuestDatabase
{
	static HashMap<Integer, QuestInstance> questDB = new HashMap<Integer, QuestInstance>();
	
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
	
	public static void writeToJSON(JsonObject jObj)
	{
		JsonArray dbJson = new JsonArray();
		
		for(QuestInstance quest : questDB.values())
		{
			JsonObject questJson = new JsonObject();
			quest.writeToJSON(questJson);
			dbJson.add(questJson);
		}
		
		jObj.add("questDatabase", dbJson);
	}
	
	public static void readFromJSON(JsonObject jObj)
	{
		questDB.clear();
		
		for(JsonElement entry : jObj.getAsJsonArray("questDatabase"))
		{
			QuestInstance quest = new QuestInstance(entry.getAsJsonObject());
			questDB.put(quest.questID, quest);
		}
	}
}
