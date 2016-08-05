package betterquesting.quests;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import betterquesting.api.database.IQuestLineDatabase;
import betterquesting.api.quests.IQuestLineContainer;
import betterquesting.api.utils.JsonHelper;

public class QuestLineDatabase implements IQuestLineDatabase
{
	public static final QuestLineDatabase INSTANCE = new QuestLineDatabase();
	
	private final ConcurrentHashMap<Integer, IQuestLineContainer> questLines = new ConcurrentHashMap<Integer, IQuestLineContainer>();
	
	private QuestLineDatabase()
	{
	}
	
	@Override
	public int getUniqueID()
	{
		int id = 0;
		
		while(questLines.containsKey(id))
		{
			id += 1;
		}
		
		return id;
	}
	
	@Override
	public void addQuestLine(IQuestLineContainer questLine)
	{
	}
	
	@Override
	public void deleteQuestLine(int lineId)
	{
	}
	
	@Override
	public IQuestLineContainer getQuestLine(int lineId)
	{
		return null;
	}
	
	@Override
	public List<IQuestLineContainer> getAllQuestLines()
	{
		return new ArrayList<IQuestLineContainer>(questLines.values());
	}
	
	@Override
	public JsonObject writeToJson(JsonObject json)
	{
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
		return null;
	}
	
	@Override
	public void readFromJson(JsonObject json)
	{
	}
	
}
