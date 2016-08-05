package betterquesting.quests;

import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.Level;
import betterquesting.api.quests.IQuestLineContainer;
import betterquesting.api.quests.IQuestLineEntry;
import betterquesting.api.utils.JsonHelper;
import betterquesting.core.BetterQuesting;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class QuestLine implements IQuestLineContainer
{
	private int ID = 0;
	public String name = "New Quest Line";
	public String description = "No description";
	public ArrayList<IQuestLineEntry> questList = new ArrayList<IQuestLineEntry>();4
	
	public QuestLine(int id)
	{
		this.ID = id;
	}
	
	@Override
	public List<IQuestLineEntry> getAllQuests()
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
		json.addProperty("id", id);
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
}
