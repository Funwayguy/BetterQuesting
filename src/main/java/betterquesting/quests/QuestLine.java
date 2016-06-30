package betterquesting.quests;

import java.util.ArrayList;
import org.apache.logging.log4j.Level;
import betterquesting.core.BetterQuesting;
import betterquesting.utils.JsonHelper;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class QuestLine
{
	public String name = "New Quest Line";
	public String description = "No description";
	public ArrayList<QuestLineEntry> questList = new ArrayList<QuestLineEntry>();
	
	public ArrayList<QuestInstance> getQuests()
	{
		ArrayList<QuestInstance> list = new ArrayList<QuestInstance>();
		
		for(QuestLineEntry entry : questList)
		{
			list.add(entry.quest);
		}
		
		return list;
	}
	
	public QuestLineEntry getEntryByID(int id)
	{
		for(QuestLineEntry entry : questList)
		{
			if(entry != null && entry.quest != null && entry.quest.questID == id)
			{
				return entry;
			}
		}
		
		return null;
	}
	
	public void writeToJSON(JsonObject json)
	{
		json.addProperty("name", name);
		json.addProperty("description", description);
		
		JsonArray jArr = new JsonArray();
		
		for(QuestLineEntry entry : questList)
		{
			JsonObject jq = new JsonObject();
			entry.writeToJson(jq);
			jArr.add(jq);
		}
		
		json.add("quests", jArr);
	}
	
	public void readFromJSON(JsonObject json)
	{
		name = JsonHelper.GetString(json, "name", "New Quest Line");
		description = JsonHelper.GetString(json, "description", "No description");
		
		//boolean remap = false;
		
		questList = new ArrayList<QuestLineEntry>();
		for(JsonElement entry : JsonHelper.GetArray(json, "quests"))
		{
			if(entry == null)
			{
				continue;
			}
			
			if(entry.isJsonPrimitive() && entry.getAsJsonPrimitive().isNumber()) // Backwards compatibility
			{
				QuestInstance quest = QuestDatabase.getQuestByID(entry.getAsInt());
				
				if(quest != null)
				{
					QuestLineEntry qe = new QuestLineEntry(quest, 0, 0);
					questList.add(qe);
				} else
				{
					BetterQuesting.logger.log(Level.WARN, "Quest line '" + this.name + "' contained an invalid entry: " + entry.toString());
				}
			} else if(entry.isJsonObject())
			{
				QuestLineEntry qe = new QuestLineEntry();
				qe.readFromJson(entry.getAsJsonObject());
				
				if(qe.quest != null)
				{
					questList.add(qe);
				} else
				{
					BetterQuesting.logger.log(Level.WARN, "Quest line '" + this.name + "' contained an invalid entry: " + entry.toString());
				}
			}
		}
	}
	
	public static class QuestLineEntry
	{
		public QuestInstance quest;
		public int posX = 0;
		public int posY = 0;
		
		protected QuestLineEntry(){}
		
		public QuestLineEntry(QuestInstance quest, int posX, int posY)
		{
			this.quest = quest;
			this.posX = posX;
			this.posY = posY;
		}
		
		public void readFromJson(JsonObject json)
		{
			quest = QuestDatabase.getQuestByID(JsonHelper.GetNumber(json, "id", -1).intValue());
			posX = JsonHelper.GetNumber(json, "x", 0).intValue();
			posY = JsonHelper.GetNumber(json, "y", 0).intValue();
		}
		
		public void writeToJson(JsonObject json)
		{
			json.addProperty("id", quest.questID);
			json.addProperty("x", posX);
			json.addProperty("y", posY);
		}
	}
}
