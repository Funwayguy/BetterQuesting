package betterquesting.questing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import betterquesting.api.enums.EnumSaveType;
import betterquesting.api.network.QuestingPacket;
import betterquesting.api.questing.IQuestLine;
import betterquesting.api.questing.IQuestLineDatabase;
import betterquesting.misc.QuestLineSortByKey;
import betterquesting.misc.QuestLineSortByValue;
import betterquesting.network.PacketTypeNative;

public final class QuestLineDatabase implements IQuestLineDatabase
{
	public static final QuestLineDatabase INSTANCE = new QuestLineDatabase();
	
	private final ConcurrentHashMap<Integer, IQuestLine> questLines = new ConcurrentHashMap<Integer, IQuestLine>();
	private final List<Integer> lineOrder = new ArrayList<Integer>();
	
	private QuestLineDatabase()
	{
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
	public IQuestLine createNew()
	{
		return new QuestLine();
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
	public boolean add(IQuestLine questLine, Integer id)
	{
		if(id < 0 || questLine == null || questLines.containsValue(questLine) || questLines.containsKey(id))
		{
			return false;
		}
		
		questLines.put(id, questLine);
		return true;
	}
	
	@Override
	public boolean removeKey(Integer lineId)
	{
		return questLines.remove(lineId) != null;
	}
	
	@Override
	public boolean removeValue(IQuestLine quest)
	{
		return removeKey(getKey(quest));
	}
	
	@Override
	public Integer getKey(IQuestLine questLine)
	{
		for(Entry<Integer,IQuestLine> entry  : questLines.entrySet())
		{
			if(entry.getValue() == questLine)
			{
				return entry.getKey();
			}
		}
		
		return -1;
	}
	
	@Override
	public IQuestLine getValue(Integer lineId)
	{
		return lineId < 0? null : questLines.get(lineId);
	}
	
	@Override
	public List<IQuestLine> getAllValues()
	{
		List<IQuestLine> list = new ArrayList<IQuestLine>(questLines.values());
		Collections.sort(list, new QuestLineSortByValue(this));
		return list;
	}
	
	@Override
	public List<Integer> getAllKeys()
	{
		List<Integer> list = new ArrayList<Integer>(((Map<Integer,IQuestLine>)questLines).keySet());
		Collections.sort(list, new QuestLineSortByKey(this));
		return list;
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
	public QuestingPacket getSyncPacket()
	{
		NBTTagCompound tags = new NBTTagCompound();
		tags.setTag("data", writeToNBT(new NBTTagList(), EnumSaveType.CONFIG));
		return new QuestingPacket(PacketTypeNative.LINE_DATABASE.GetLocation(), tags);
	}
	
	@Override
	public void readPacket(NBTTagCompound payload)
	{
		this.readFromNBT(payload.getTagList("data", 10), EnumSaveType.CONFIG);
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
		ArrayList<IQuestLine> unassigned = new ArrayList<IQuestLine>();
		
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
			} else
			{
				unassigned.add(line);
			}
			
			if(order >= 0)
			{
				orderMap.put(order, id);
			}
		}
		
		// Legacy support ONLY
		for(IQuestLine q : unassigned)
		{
			questLines.put(this.nextKey(), q);
		}
		
		List<Integer> orderKeys = new ArrayList<Integer>(orderMap.keySet());
		Collections.sort(orderKeys);
		
		lineOrder.clear();
		for(int o : orderKeys)
		{
			lineOrder.add(orderMap.get(o));
		}
	}
}
