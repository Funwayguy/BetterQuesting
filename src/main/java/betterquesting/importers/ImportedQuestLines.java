package betterquesting.importers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import net.minecraft.nbt.NBTTagCompound;
import betterquesting.api.enums.EnumSaveType;
import betterquesting.api.network.QuestingPacket;
import betterquesting.api.questing.IQuestLine;
import betterquesting.api.questing.IQuestLineDatabase;
import betterquesting.api.utils.JsonHelper;
import betterquesting.quests.QuestLine;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class ImportedQuestLines implements IQuestLineDatabase
{
	private final HashMap<Integer, IQuestLine> questLines = new HashMap<Integer, IQuestLine>();
	
	@Override
	public Integer nextKey()
	{
		int id = 0;
		
		while(questLines.containsKey(id))
		{
			id += 1;
		}
		
		return id;
	}
	
	@Override
	public boolean add(IQuestLine value, Integer key)
	{
		if(key < 0 || value == null || questLines.containsValue(value) || questLines.containsKey(key))
		{
			return false;
		}
		
		questLines.put(key, value);
		return true;
	}
	
	@Override
	public boolean removeKey(Integer key)
	{
		return questLines.remove(key) != null;
	}
	
	@Override
	public boolean removeValue(IQuestLine value)
	{
		return removeKey(getKey(value));
	}
	
	@Override
	public IQuestLine getValue(Integer key)
	{
		return questLines.get(key);
	}
	
	@Override
	public Integer getKey(IQuestLine value)
	{
		for(Entry<Integer,IQuestLine> entry  : questLines.entrySet())
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
		return questLines.size();
	}
	
	@Override
	public void reset()
	{
		questLines.clear();
	}
	
	@Override
	public List<IQuestLine> getAllValues()
	{
		return new ArrayList<IQuestLine>(questLines.values());
	}
	
	@Override
	public List<Integer> getAllKeys()
	{
		return new ArrayList<Integer>(questLines.keySet());
	}
	
	@Override
	public JsonArray writeToJson(JsonArray json, EnumSaveType saveType)
	{
		if(saveType != EnumSaveType.CONFIG)
		{
			return json;
		}
		
		for(Entry<Integer,IQuestLine> entry : questLines.entrySet())
		{
			if(entry.getValue() == null)
			{
				continue;
			}
			
			JsonObject jObj = entry.getValue().writeToJson(new JsonObject(), saveType);
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
		
		questLines.clear();
		
		for(JsonElement entry : json)
		{
			if(entry == null || !entry.isJsonObject())
			{
				continue;
			}
			
			JsonObject jql = entry.getAsJsonObject();
			
			int id = JsonHelper.GetNumber(jql, "lineID", -1).intValue();
			QuestLine line = new QuestLine();
			line.readFromJson(entry.getAsJsonObject(), saveType);
			
			if(id >= 0)
			{
				questLines.put(id, line);
			}
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
	public void removeQuest(int questID)
	{
		for(IQuestLine ql : getAllValues())
		{
			ql.removeKey(questID);
		}
	}
	
	@Override
	public IQuestLine createNew()
	{
		return new QuestLine();
	}
}
