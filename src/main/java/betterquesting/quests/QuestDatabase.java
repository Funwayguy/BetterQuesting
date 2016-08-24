package betterquesting.quests;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.MinecraftForge;
import betterquesting.api.database.IQuestDatabase;
import betterquesting.api.enums.EnumSaveType;
import betterquesting.api.events.QuestDataEvent;
import betterquesting.api.network.PacketTypeNative;
import betterquesting.api.network.PreparedPayload;
import betterquesting.api.quests.IQuestContainer;
import betterquesting.api.utils.JsonHelper;
import betterquesting.api.utils.NBTConverter;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

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
		if(id < 0 || obj == null || database.containsKey(id) || database.containsValue(obj))
		{
			return false;
		}
		
		database.put(id, obj);
		return true;
	}
	
	@Override
	public boolean remove(int id)
	{
		boolean flag = database.remove(id) != null;
		
		if(flag)
		{
			// Clear quest from quest lines
			QuestLineDatabase.INSTANCE.removeQuest(id);
		}
		
		return flag;
	}
	
	@Override
	public boolean remove(IQuestContainer quest)
	{
		int id = getKey(quest);
		
		return remove(id);
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
	public void reset()
	{
		database.clear();
	}
	
	@Override
	public PreparedPayload getSyncPacket()
	{
		NBTTagCompound tags = new NBTTagCompound();
		JsonObject base = new JsonObject();
		base.add("config", writeToJson(new JsonArray(), EnumSaveType.CONFIG));
		base.add("progress", writeToJson(new JsonArray(), EnumSaveType.PROGRESS));
		tags.setTag("data", NBTConverter.JSONtoNBT_Object(base, new NBTTagCompound()));
		return new PreparedPayload(PacketTypeNative.QUEST_DATABASE.GetLocation(), tags);
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
		
		MinecraftForge.EVENT_BUS.post(new QuestDataEvent.DatabaseUpdated());
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
