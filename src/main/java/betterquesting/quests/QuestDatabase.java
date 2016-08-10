package betterquesting.quests;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.common.MinecraftForge;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import betterquesting.api.database.IQuestDatabase;
import betterquesting.api.enums.EnumSaveType;
import betterquesting.api.events.QuestDataEvent;
import betterquesting.api.events.QuestDataEvent.EventDatabase;
import betterquesting.api.quests.IQuestContainer;
import betterquesting.api.utils.JsonHelper;

public final class QuestDatabase implements IQuestDatabase
{
	public static final QuestDatabase INSTANCE = new QuestDatabase();
	
	private final ConcurrentHashMap<Integer, IQuestContainer> database = new ConcurrentHashMap<Integer, IQuestContainer>();
	
	private QuestDatabase()
	{
	}
	
	@Override
	public int nextID()
	{
		int id = 0;
		
		while(database.containsKey(id))
		{
			id++;
		}
		
		return id;
	}
	
	@Override
	public boolean add(IQuestContainer obj, int id)
	{
		if(obj == null || database.containsKey(id) || database.containsValue(obj))
		{
			return false;
		}
		
		database.put(id, obj);
		return true;
	}
	
	@Override
	public boolean remove(int id)
	{
		return database.remove(id) != null;
	}
	
	@Override
	public IQuestContainer getValue(int id)
	{
		return database.get(id);
	}
	
	@Override
	public int getKey(IQuestContainer quest)
	{
		for(Entry<Integer,IQuestContainer> entry : database.entrySet())
		{
			if(entry.getValue() == quest)
			{
				return entry.getKey();
			}
		}
		
		return -1;
	}
	
	@Override
	public List<IQuestContainer> getAllValues()
	{
		return new ArrayList<IQuestContainer>(database.values());
	}
	
	@Override
	public List<Integer> getAllKeys()
	{
		return new ArrayList<Integer>(database.keySet());
	}
	
	@Override
	public int size()
	{
		return database.size();
	}
	
	@Override
	public void syncAll()
	{
	}
	
	@Override
	public void syncPlayer(EntityPlayerMP player)
	{
	}
	
	@Override
	public JsonArray writeToJson(JsonArray json, EnumSaveType saveType)
	{
		switch(saveType)
		{
			case CONFIG:
				writeToJson_Config(json);
				break;
			case PROGRESS:
				writeToJson_Progress(json);
				break;
			default:
				break;
		}
		
		return json;
	}
	
	@Override
	public void readFromJson(JsonArray json, EnumSaveType saveType)
	{
		switch(saveType)
		{
			case CONFIG:
				readFromJson_Config(json);
				break;
			case PROGRESS:
				readFromJson_Progress(json);
				break;
			default:
				break;
		}
		
		MinecraftForge.EVENT_BUS.post(new QuestDataEvent.DatabaseUpdateEvent(EventDatabase.QUEST_MAIN));
	}
	
	private JsonArray writeToJson_Config(JsonArray json)
	{
		for(Entry<Integer,IQuestContainer> entry : database.entrySet())
		{
			JsonObject jq = new JsonObject();
			entry.getValue().writeToJson(jq, EnumSaveType.CONFIG);
			jq.addProperty("questID", entry.getKey());
			json.add(jq);
		}
		
		return json;
	}
	
	private void readFromJson_Config(JsonArray json)
	{
		database.clear();
		for(JsonElement entry : json)
		{
			if(entry == null || !entry.isJsonObject())
			{
				continue;
			}
			
			int qID = JsonHelper.GetNumber(entry.getAsJsonObject(), "questID", -1).intValue();
			
			if(qID < 0)
			{
				continue;
			}
			
			IQuestContainer quest = getValue(qID);
			quest = quest != null? quest : new QuestInstance();
			quest.readFromJson(entry.getAsJsonObject(), EnumSaveType.CONFIG);
		}
	}
	
	private JsonArray writeToJson_Progress(JsonArray json)
	{
		for(Entry<Integer,IQuestContainer> entry : database.entrySet())
		{
			JsonObject jq = new JsonObject();
			entry.getValue().writeToJson(jq, EnumSaveType.PROGRESS);
			jq.addProperty("questID", entry.getKey());
			json.add(jq);
		}
		
		return json;
	}
	
	private void readFromJson_Progress(JsonArray json)
	{
		for(JsonElement entry : json)
		{
			if(entry == null || !entry.isJsonObject())
			{
				continue;
			}
			
			int qID = JsonHelper.GetNumber(entry.getAsJsonObject(), "questID", -1).intValue();
			
			if(qID < 0)
			{
				continue;
			}
			
			IQuestContainer quest = getValue(qID);
			quest = quest != null? quest : new QuestInstance();
			quest.readFromJson(entry.getAsJsonObject(), EnumSaveType.PROGRESS);
		}
	}
}
