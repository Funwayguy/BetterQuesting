package betterquesting.client.importers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import betterquesting.api.enums.EnumSaveType;
import betterquesting.api.network.QuestingPacket;
import betterquesting.api.questing.IQuest;
import betterquesting.api.questing.IQuestDatabase;
import betterquesting.questing.QuestInstance;

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
		
		value.setParentDatabase(this);
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
	public NBTTagList writeToNBT(NBTTagList json, EnumSaveType saveType)
	{
		if(saveType != EnumSaveType.CONFIG)
		{
			return json;
		}
		
		for(Entry<Integer,IQuest> entry : database.entrySet())
		{
			NBTTagCompound jq = new NBTTagCompound();
			entry.getValue().writeToNBT(jq, saveType);
			jq.setInteger("questID", entry.getKey());
			json.appendTag(jq);
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
		
		database.clear();
		for(int i = 0; i < json.tagCount(); i++)
		{
			NBTBase entry = json.get(i);
			
			if(entry == null || entry.getId() != 10)
			{
				continue;
			}
			
			NBTTagCompound qTag = (NBTTagCompound)entry;
			
			int qID = qTag.hasKey("questID", 99) ? qTag.getInteger("questID") : -1;
			
			if(qID < 0)
			{
				continue;
			}
			
			IQuest quest = getValue(qID);
			quest = quest != null? quest : this.createNew();
			quest.readFromNBT(qTag, EnumSaveType.CONFIG);
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
		IQuest q = new QuestInstance();
		q.setParentDatabase(this);
		return q;
	}
}
