package betterquesting.quests;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import betterquesting.api.database.IQuestLineDatabase;
import betterquesting.api.enums.EnumSaveType;
import betterquesting.api.quests.IQuestLineContainer;
import betterquesting.api.utils.JsonHelper;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public final class QuestLineDatabase implements IQuestLineDatabase
{
	public static final QuestLineDatabase INSTANCE = new QuestLineDatabase();
	
	private final ConcurrentHashMap<Integer, IQuestLineContainer> questLines = new ConcurrentHashMap<Integer, IQuestLineContainer>();
	
	private QuestLineDatabase()
	{
	}
	
	@Override
	public int nextID()
	{
		int id = 0;
		
		while(questLines.containsKey(id))
		{
			id += 1;
		}
		
		return id;
	}
	
	@Override
	public boolean add(IQuestLineContainer questLine, int id)
	{
		if(questLine == null || id < 0 || questLines.containsValue(questLine) || questLines.containsKey(id))
		{
			return false;
		}
		
		questLines.put(id, questLine);
		return true;
	}
	
	@Override
	public boolean remove(int lineId)
	{
		return questLines.remove(lineId) != null;
	}
	
	@Override
	public int getKey(IQuestLineContainer questLine)
	{
		for(Entry<Integer,IQuestLineContainer> entry  : questLines.entrySet())
		{
			if(entry.getValue() == questLine)
			{
				return entry.getKey();
			}
		}
		
		return -1;
	}
	
	@Override
	public IQuestLineContainer getValue(int lineId)
	{
		return questLines.get(lineId);
	}
	
	@Override
	public List<IQuestLineContainer> getAllValues()
	{
		return new ArrayList<IQuestLineContainer>(questLines.values());
	}
	
	@Override
	public List<Integer> getAllKeys()
	{
		return new ArrayList<Integer>(questLines.keySet());
	}
	
	@Override
	public int size()
	{
		return questLines.size();
	}
	
	@Override
	public void syncDatabase()
	{
		//TODO: Setup dedicated sync packet
	}
	
	@Override
	public JsonArray writeToJson(JsonArray json, EnumSaveType saveType)
	{
		if(saveType != EnumSaveType.CONFIG)
		{
			return json;
		}
		
		for(Entry<Integer,IQuestLineContainer> entry : questLines.entrySet())
		{
			if(entry.getValue() == null)
			{
				continue;
			}
			
			JsonObject jObj = entry.getValue().writeToJson(new JsonObject());
			jObj.addProperty("lineID", entry.getKey());
			json.add(jObj);
		}
		
		return json;
	}
	
	@Override
	public void readFromJson(JsonArray json, EnumSaveType saveType)
	{
		if(saveType != EnumSaveType.CONFIG)
		{
			return;
		}
		
		ArrayList<IQuestLineContainer> unassigned = new ArrayList<IQuestLineContainer>();
		
		for(JsonElement entry : json)
		{
			if(entry == null || !entry.isJsonObject())
			{
				continue;
			}
			
			JsonObject jql = entry.getAsJsonObject();
			
			int id = JsonHelper.GetNumber(jql, "lineID", -1).intValue();
			QuestLine line = new QuestLine();
			line.readFromJson(entry.getAsJsonObject());
			
			if(id >= 0)
			{
				questLines.put(id, line);
			} else
			{
				unassigned.add(line);
			}
		}
		
		// Legacy support ONLY
		for(IQuestLineContainer q : unassigned)
		{
			questLines.put(this.nextID(), q);
		}
	}
}
