package betterquesting.client.importers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import betterquesting.api.enums.EnumSaveType;
import betterquesting.api.network.QuestingPacket;
import betterquesting.api.questing.IQuestLine;
import betterquesting.api.questing.IQuestLineDatabase;
import betterquesting.questing.QuestLine;

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
	public NBTTagList writeToNBT(NBTTagList json, EnumSaveType saveType)
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
			
			NBTTagCompound jObj = entry.getValue().writeToNBT(new NBTTagCompound(), saveType);
			jObj.setInteger("lineID", id);
			jObj.setInteger("order", getOrderIndex(id));
			json.appendTag(jObj);
		}
		
		return json;
	}
	
	@Override
	public void readFromNBT(NBTTagList json, EnumSaveType saveType)
	{
		if(saveType != EnumSaveType.CONFIG)
		{
			return;
		}
		
		questLines.clear();
		HashMap<Integer,Integer> orderMap = new HashMap<Integer,Integer>();
		
		for(int i = 0; i < json.tagCount(); i++)
		{
			NBTBase entry = json.get(i);
			
			if(entry == null || entry.getId() != 10)
			{
				continue;
			}
			
			NBTTagCompound jql = (NBTTagCompound)entry;
			
			int id = jql.hasKey("lineID", 99) ? jql.getInteger("lineID") : -1;
			int order = jql.hasKey("order", 99) ? jql.getInteger("order") : -1;
			QuestLine line = new QuestLine();
			line.readFromNBT(jql, saveType);
			
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
