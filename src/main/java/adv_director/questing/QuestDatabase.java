package adv_director.questing;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.nbt.NBTTagCompound;
import adv_director.api.enums.EnumSaveType;
import adv_director.api.network.QuestingPacket;
import adv_director.api.questing.IQuest;
import adv_director.api.questing.IQuestDatabase;
import adv_director.api.utils.JsonHelper;
import adv_director.api.utils.NBTConverter;
import adv_director.network.PacketTypeNative;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

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
		JsonObject base = new JsonObject();
		base.add("config", writeToJson(new JsonArray(), EnumSaveType.CONFIG));
		base.add("progress", writeToJson(new JsonArray(), EnumSaveType.PROGRESS));
		tags.setTag("data", NBTConverter.JSONtoNBT_Object(base, new NBTTagCompound()));
		return new QuestingPacket(PacketTypeNative.QUEST_DATABASE.GetLocation(), tags);
	}
	
	@Override
	public void readPacket(NBTTagCompound payload)
	{
		JsonObject base = NBTConverter.NBTtoJSON_Compound(payload.getCompoundTag("data"), new JsonObject());
		
		readFromJson(JsonHelper.GetArray(base, "config"), EnumSaveType.CONFIG);
		readFromJson(JsonHelper.GetArray(base, "progress"), EnumSaveType.PROGRESS);
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
	}
	
	private JsonArray writeToJson_Config(JsonArray json)
	{
		for(Entry<Integer,IQuest> entry : database.entrySet())
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
			
			IQuest quest = getValue(qID);
			quest = quest != null? quest : this.createNew();
			quest.readFromJson(entry.getAsJsonObject(), EnumSaveType.CONFIG);
			database.put(qID, quest);
		}
	}
	
	private JsonArray writeToJson_Progress(JsonArray json)
	{
		for(Entry<Integer,IQuest> entry : database.entrySet())
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
			
			IQuest quest = getValue(qID);
			
			if(quest != null)
			{
				quest.readFromJson(entry.getAsJsonObject(), EnumSaveType.PROGRESS);
			}
		}
	}
}
