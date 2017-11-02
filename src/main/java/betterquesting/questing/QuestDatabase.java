package betterquesting.questing;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import betterquesting.api.enums.EnumSaveType;
import betterquesting.api.network.QuestingPacket;
import betterquesting.api.questing.IQuest;
import betterquesting.api.questing.IQuestDatabase;
import betterquesting.network.PacketTypeNative;

public final class QuestDatabase implements IQuestDatabase
{
	public static final QuestDatabase INSTANCE = new QuestDatabase();
	
	private final ConcurrentHashMap<Integer, IQuest> database = new ConcurrentHashMap<Integer, IQuest>();
	
	private QuestDatabase()
	{
	}
	
	@Override
	public IQuest createNew()
	{
		IQuest q = new QuestInstance();
		q.setParentDatabase(this);
		return q;
	}
	
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
	public boolean add(IQuest obj, Integer id)
	{
		if(id < 0 || obj == null || database.containsKey(id) || database.containsValue(obj))
		{
			return false;
		}
		
		obj.setParentDatabase(this);
		database.put(id, obj);
		return true;
	}
	
	@Override
	public boolean removeKey(Integer id)
	{
		IQuest remQ = database.remove(id);
		
		if(remQ == null)
		{
			return false;
		}
		
		for(IQuest quest : this.getAllValues())
		{
			// Remove from all pre-requisites
			quest.getPrerequisites().remove(remQ);
		}
		
		// Clear quest from quest lines
		QuestLineDatabase.INSTANCE.removeQuest(id);
		
		return true;
	}
	
	@Override
	public boolean removeValue(IQuest quest)
	{
		return removeKey(getKey(quest));
	}
	
	@Override
	public IQuest getValue(Integer id)
	{
		return database.get(id);
	}
	
	@Override
	public Integer getKey(IQuest quest)
	{
		for(Entry<Integer,IQuest> entry : database.entrySet())
		{
			if(entry.getValue() == quest)
			{
				return entry.getKey();
			}
		}
		
		return -1;
	}
	
	@Override
	public List<IQuest> getAllValues()
	{
		return new ArrayList<IQuest>(database.values());
	}
	
	@Override
	public List<Integer> getAllKeys()
	{
		return new ArrayList<Integer>(((Map<Integer,IQuest>)database).keySet());
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
	public QuestingPacket getSyncPacket()
	{
		NBTTagCompound tags = new NBTTagCompound();
		NBTTagCompound base = new NBTTagCompound();
		base.setTag("config", writeToNBT(new NBTTagList(), EnumSaveType.CONFIG));
		base.setTag("progress", writeToNBT(new NBTTagList(), EnumSaveType.PROGRESS));
		tags.setTag("data", base);
		return new QuestingPacket(PacketTypeNative.QUEST_DATABASE.GetLocation(), tags);
	}
	
	@Override
	public void readPacket(NBTTagCompound payload)
	{
		NBTTagCompound base = payload.getCompoundTag("data");
		
		readFromNBT(base.getTagList("config", 10), EnumSaveType.CONFIG);
		readFromNBT(base.getTagList("progress", 10), EnumSaveType.PROGRESS);
	}
	
	@Override
	public NBTTagList writeToNBT(NBTTagList json, EnumSaveType saveType)
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
	public void readFromNBT(NBTTagList json, EnumSaveType saveType)
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
	}
	
	private NBTTagList writeToJson_Config(NBTTagList json)
	{
		for(Entry<Integer,IQuest> entry : database.entrySet())
		{
			NBTTagCompound jq = new NBTTagCompound();
			entry.getValue().writeToNBT(jq, EnumSaveType.CONFIG);
			jq.setInteger("questID", entry.getKey());
			json.appendTag(jq);
		}
		
		return json;
	}
	
	private void readFromJson_Config(NBTTagList json)
	{
		database.clear();
		System.out.println("Loading " + json.tagCount() + " from NBTTagList");
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
	
	private NBTTagList writeToJson_Progress(NBTTagList json)
	{
		for(Entry<Integer,IQuest> entry : database.entrySet())
		{
			NBTTagCompound jq = new NBTTagCompound();
			entry.getValue().writeToNBT(jq, EnumSaveType.PROGRESS);
			jq.setInteger("questID", entry.getKey());
			json.appendTag(jq);
		}
		System.out.println("Saved " + json.tagCount() + " to NBTTagList");
		
		return json;
	}
	
	private void readFromJson_Progress(NBTTagList json)
	{
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
			
			if(quest != null)
			{
				quest.readFromNBT(qTag, EnumSaveType.PROGRESS);
			}
		}
	}
}
