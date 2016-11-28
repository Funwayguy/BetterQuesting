package betterquesting.client.importers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import net.minecraft.nbt.NBTTagCompound;
import betterquesting.api.enums.EnumSaveType;
import betterquesting.api.network.QuestingPacket;
import betterquesting.api.questing.IQuestLine;
import betterquesting.api.questing.IQuestLineDatabase;
import betterquesting.api.utils.JsonHelper;
import betterquesting.questing.QuestLine;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class ImportedQuestLines implements IQuestLineDatabase
{
	private final HashMap<Integer, IQuestLine> questLines = new HashMap<Integer, IQuestLine>();
	private final List<Integer> lineOrder = new ArrayList<Integer>();
	
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
	public int getOrderIndex(int lineID)
	{
		if(!questLines.containsKey(lineID))
		{
			return -1;
		} else if(!lineOrder.contains(lineID))
		{
			lineOrder.add(lineID);
		}
		
		return lineOrder.indexOf(lineID);
	}
	
	@Override
	public void setOrderIndex(int lineID, int index)
	{
		lineOrder.remove((Integer)lineID);
		lineOrder.add(index, lineID);
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
			
			int id = entry.getKey();
			
			JsonObject jObj = entry.getValue().writeToJson(new JsonObject(), saveType);
			jObj.addProperty("lineID", id);
			jObj.addProperty("order", getOrderIndex(id));
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
		HashMap<Integer,Integer> orderMap = new HashMap<Integer,Integer>();
		
		for(JsonElement entry : json)
		{
			if(entry == null || !entry.isJsonObject())
			{
				continue;
			}
			
			JsonObject jql = entry.getAsJsonObject();
			
			int id = JsonHelper.GetNumber(jql, "lineID", -1).intValue();
			int order = JsonHelper.GetNumber(jql, "order", -1).intValue();
			QuestLine line = new QuestLine();
			line.readFromJson(entry.getAsJsonObject(), saveType);
			
			if(id >= 0)
			{
				questLines.put(id, line);
			}
			
			if(order >= 0)
			{
				orderMap.put(order, id);
			}
		}
		
		List<Integer> orderKeys = new ArrayList<Integer>(orderMap.keySet());
		Collections.sort(orderKeys);
		
		lineOrder.clear();
		for(int o : orderKeys)
		{
			lineOrder.add(orderMap.get(o));
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
		QuestLine ql = new QuestLine();
		ql.setParentDatabase(this);
		return ql;
	}
}
