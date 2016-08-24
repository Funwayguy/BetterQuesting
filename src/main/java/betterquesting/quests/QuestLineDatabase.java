package betterquesting.quests;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.nbt.NBTTagCompound;
import betterquesting.api.database.IQuestLineDatabase;
import betterquesting.api.enums.EnumSaveType;
import betterquesting.api.network.PacketTypeNative;
import betterquesting.api.network.PreparedPayload;
import betterquesting.api.quests.IQuestLineContainer;
import betterquesting.api.utils.JsonHelper;
import betterquesting.api.utils.NBTConverter;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public final class QuestLineDatabase implements IQuestLineDatabase
{
	public static final QuestLineDatabase INSTANCE = new QuestLineDatabase();
	
	/** 
	 * NOTE: The keys used in this database represent questIDs and are NOT unique identifiers
	 */
	private final ConcurrentHashMap<Integer, IQuestLineContainer> questLines = new ConcurrentHashMap<Integer, IQuestLineContainer>();
	
	private QuestLineDatabase()
	{
	}
	
	@Override
	public void removeQuest(int questID)
	{
		for(IQuestLineContainer ql : getAllValues())
		{
			ql.remove(questID);
		}
	}
	
	@Override
	public int nextID()
	{
		int id = 0;
		
		while(questLines.containsKey(id))
		{
			id += 1;
		}
		
		return id;
	}
	
	@Override
	public boolean add(IQuestLineContainer questLine, int id)
	{
		if(id < 0 || questLine == null || questLines.containsValue(questLine) || questLines.containsKey(id))
		{
			return false;
		}
		
		questLines.put(id, questLine);
		return true;
	}
	
	@Override
	public boolean remove(int lineId)
	{
		return questLines.remove(lineId) != null;
	}
	
	@Override
	public boolean remove(IQuestLineContainer quest)
	{
		return remove(getKey(quest));
	}
	
	@Override
	public int getKey(IQuestLineContainer questLine)
	{
		for(Entry<Integer,IQuestLineContainer> entry  : questLines.entrySet())
		{
			if(entry.getValue() == questLine)
			{
				return entry.getKey();
			}
		}
		
		return -1;
	}
	
	@Override
	public IQuestLineContainer getValue(int lineId)
	{
		return questLines.get(lineId);
	}
	
	@Override
	public List<IQuestLineContainer> getAllValues()
	{
		return new ArrayList<IQuestLineContainer>(questLines.values());
	}
	
	@Override
	public List<Integer> getAllKeys()
	{
		return new ArrayList<Integer>(questLines.keySet());
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
	public PreparedPayload getSyncPacket()
	{
		NBTTagCompound tags = new NBTTagCompound();
		JsonObject base = new JsonObject();
		base.add("questLines", writeToJson(new JsonArray(), EnumSaveType.CONFIG));
		tags.setTag("data", NBTConverter.JSONtoNBT_Object(base, new NBTTagCompound()));
		return new PreparedPayload(PacketTypeNative.LINE_DATABASE.GetLocation(), tags);
	}
	
	@Override
	public void readPacket(NBTTagCompound payload)
	{
		JsonObject base = NBTConverter.NBTtoJSON_Compound(payload.getCompoundTag("data"), new JsonObject());
		
		this.readFromJson(JsonHelper.GetArray(base, "questLines"), EnumSaveType.CONFIG);
	}
	
	@Override
	public JsonArray writeToJson(JsonArray json, EnumSaveType saveType)
	{
		if(saveType != EnumSaveType.CONFIG)
		{
			return json;
		}
		
		for(Entry<Integer,IQuestLineContainer> entry : questLines.entrySet())
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
		
		ArrayList<IQuestLineContainer> unassigned = new ArrayList<IQuestLineContainer>();
		
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
			} else
			{
				unassigned.add(line);
			}
		}
		
		// Legacy support ONLY
		for(IQuestLineContainer q : unassigned)
		{
			questLines.put(this.nextID(), q);
		}
	}
}
