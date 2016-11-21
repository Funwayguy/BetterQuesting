package betterquesting.importers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import net.minecraft.nbt.NBTTagCompound;
import betterquesting.api.enums.EnumSaveType;
import betterquesting.api.network.QuestingPacket;
import betterquesting.api.questing.IQuest;
import betterquesting.api.questing.IQuestDatabase;
import betterquesting.api.utils.JsonHelper;
import betterquesting.quests.QuestInstance;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class ImportedQuests implements IQuestDatabase
{
	private final HashMap<Integer, IQuest> database = new HashMap<Integer, IQuest>();
	
	@Override
	public Integer nextKey()
	{
		int id = 0;
		
		while(database.containsKey(id))
		{
			id++;
		}
		
		return id;
	}
	
	@Override
	public boolean add(IQuest value, Integer key)
	{
		if(key < 0 || value == null || database.containsKey(key) || database.containsValue(value))
		{
			return false;
		}
		
		database.put(key, value);
		return true;
	}
	
	@Override
	public boolean removeKey(Integer key)
	{
		return database.remove(key) != null;
	}
	
	@Override
	public boolean removeValue(IQuest value)
	{
		return database.remove(getKey(value)) != null;
	}
	
	@Override
	public IQuest getValue(Integer key)
	{
		return database.get(key);
	}
	
	@Override
	public Integer getKey(IQuest value)
	{
		for(Entry<Integer,IQuest> entry : database.entrySet())
		{
			if(entry.getValue() == value)
			{
				return entry.getKey();
			}
		}
		
		return -1;
	}
	
	@Override
	public int size()
	{
		return database.size();
	}
	
	@Override
	public void reset()
	{
		database.clear();
	}
	
	@Override
	public List<IQuest> getAllValues()
	{
		return new ArrayList<IQuest>(database.values());
	}
	
	@Override
	public List<Integer> getAllKeys()
	{
		return new ArrayList<Integer>(database.keySet());
	}
	
	@Override
	public JsonArray writeToJson(JsonArray json, EnumSaveType saveType)
	{
		if(saveType != EnumSaveType.CONFIG)
		{
			return json;
		}
		
		for(Entry<Integer,IQuest> entry : database.entrySet())
		{
			JsonObject jq = new JsonObject();
			entry.getValue().writeToJson(jq, saveType);
			jq.addProperty("questID", entry.getKey());
			json.add(jq);
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
			
			IQuest quest = getValue(qID);
			quest = quest != null? quest : new QuestInstance();
			quest.readFromJson(entry.getAsJsonObject(), EnumSaveType.CONFIG);
			database.put(qID, quest);
		}
	}
	
	@Override
	public QuestingPacket getSyncPacket()
	{
		return null;
	}
	
	@Override
	public void readPacket(NBTTagCompound payload)
	{
	}
	
	@Override
	public IQuest createNew()
	{
		return new QuestInstance();
	}
}
