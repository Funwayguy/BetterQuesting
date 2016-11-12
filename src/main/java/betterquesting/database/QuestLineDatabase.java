package betterquesting.database;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.nbt.NBTTagCompound;
import betterquesting.api.database.IQuestLineDatabase;
import betterquesting.api.enums.EnumSaveType;
import betterquesting.api.network.PacketTypeNative;
import betterquesting.api.network.PreparedPayload;
import betterquesting.api.quests.IQuestLine;
import betterquesting.api.utils.JsonHelper;
import betterquesting.api.utils.NBTConverter;
import betterquesting.quests.QuestLine;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public final class QuestLineDatabase implements IQuestLineDatabase
{
	public static final QuestLineDatabase INSTANCE = new QuestLineDatabase();
	
	/** 
	 * NOTE: The keys used in this database represent questIDs and are NOT unique identifiers
	 */
	private final ConcurrentHashMap<Integer, IQuestLine> questLines = new ConcurrentHashMap<Integer, IQuestLine>();
	
	private QuestLineDatabase()
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
		return questLines.get(lineId);
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
		
		ArrayList<IQuestLine> unassigned = new ArrayList<IQuestLine>();
		
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
		for(IQuestLine q : unassigned)
		{
			questLines.put(this.nextKey(), q);
		}
	}
}
