package adv_director.questing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import net.minecraft.nbt.NBTTagCompound;
import adv_director.api.api.ApiReference;
import adv_director.api.api.QuestingAPI;
import adv_director.api.enums.EnumSaveType;
import adv_director.api.network.QuestingPacket;
import adv_director.api.properties.IPropertyContainer;
import adv_director.api.properties.IPropertyType;
import adv_director.api.properties.NativeProps;
import adv_director.api.questing.IQuestLine;
import adv_director.api.questing.IQuestLineDatabase;
import adv_director.api.questing.IQuestLineEntry;
import adv_director.api.utils.JsonHelper;
import adv_director.api.utils.NBTConverter;
import adv_director.network.PacketTypeNative;
import adv_director.storage.PropertyContainer;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class QuestLine implements IQuestLine
{
	private IPropertyContainer info = new PropertyContainer();
	private final HashMap<Integer,IQuestLineEntry> questList = new HashMap<Integer,IQuestLineEntry>();
	
	private IQuestLineDatabase parentDB;
	
	public QuestLine()
	{
		parentDB = QuestingAPI.getAPI(ApiReference.LINE_DB);
		
		setupProps();
	}
	
	private void setupProps()
	{
		this.setupValue(NativeProps.NAME, "New Quest Line");
		this.setupValue(NativeProps.DESC, "No Description");
		this.setupValue(NativeProps.BG_IMAGE);
		this.setupValue(NativeProps.BG_SIZE);
	}
	
	private <T> void setupValue(IPropertyType<T> prop)
	{
		this.setupValue(prop, prop.getDefault());
	}
	
	private <T> void setupValue(IPropertyType<T> prop, T def)
	{
		info.setProperty(prop, info.getProperty(prop, def));
	}
	
	@Override
	public void setParentDatabase(IQuestLineDatabase lineDB)
	{
		this.parentDB = lineDB;
	}
	
	@Override
	public String getUnlocalisedName()
	{
		String def = "New Quest Line";
		
		if(!info.hasProperty(NativeProps.NAME))
		{
			info.setProperty(NativeProps.NAME, def);
			return def;
		}
		
		return info.getProperty(NativeProps.NAME, def);
	}
	
	@Override
	public String getUnlocalisedDescription()
	{
		String def = "No Description";
		
		if(!info.hasProperty(NativeProps.DESC))
		{
			info.setProperty(NativeProps.DESC, def);
			return def;
		}
		
		return info.getProperty(NativeProps.DESC, def);
	}
	
	@Override
	public IQuestLineEntry createNewEntry()
	{
		return new QuestLineEntry(0, 0, 24);
	}
	
	@Override
	public IPropertyContainer getProperties()
	{
		return info;
	}
	
	@Override
	public int getQuestAt(int x, int y)
	{
		for(Entry<Integer,IQuestLineEntry> entry : questList.entrySet())
		{
			int i1 = entry.getValue().getPosX();
			int j1 = entry.getValue().getPosY();
			int i2 = i1 + entry.getValue().getSize();
			int j2 = j1 + entry.getValue().getSize();
			
			if(x >= i1 && x < i2 && y >= j1 && y < j2)
			{
				return entry.getKey();
			}
		}
		
		return -1;
	}
	
	/**
	 * Use <i>QuestDatabase.INSTANCE.nextID()</i>
	 */
	@Override
	@Deprecated
	public Integer nextKey()
	{
		return -1;
	}
	
	@Override
	public boolean add(IQuestLineEntry entry, Integer questID)
	{
		if(questID < 0 || entry == null || questList.containsKey(questID) || questList.containsValue(entry))
		{
			return false;
		}
		
		questList.put(questID, entry);
		return true;
	}
	
	@Override
	public boolean removeKey(Integer questID)
	{
		return questList.remove(questID) != null;
	}
	
	@Override
	public boolean removeValue(IQuestLineEntry entry)
	{
		return removeKey(getKey(entry));
	}
	
	@Override
	public IQuestLineEntry getValue(Integer questID)
	{
		return questList.get(questID);
	}
	
	@Override
	public Integer getKey(IQuestLineEntry entry)
	{
		for(Entry<Integer,IQuestLineEntry> list : questList.entrySet())
		{
			if(list.getValue() == entry)
			{
				return list.getKey();
			}
		}
		
		return -1;
	}
	
	@Override
	public List<IQuestLineEntry> getAllValues()
	{
		return new ArrayList<IQuestLineEntry>(questList.values());
	}
	
	@Override
	public List<Integer> getAllKeys()
	{
		return new ArrayList<Integer>(questList.keySet());
	}
	
	@Override
	public int size()
	{
		return questList.size();
	}
	
	@Override
	public void reset()
	{
		questList.clear();
	}
	
	@Override
	public QuestingPacket getSyncPacket()
	{
		NBTTagCompound tags = new NBTTagCompound();
		JsonObject base = new JsonObject();
		base.add("line", writeToJson(new JsonObject(), EnumSaveType.CONFIG));
		tags.setTag("data", NBTConverter.JSONtoNBT_Object(base, new NBTTagCompound()));
		tags.setInteger("lineID", parentDB.getKey(this));
		
		return new QuestingPacket(PacketTypeNative.LINE_SYNC.GetLocation(), tags);
	}
	
	@Override
	public void readPacket(NBTTagCompound payload)
	{
		JsonObject base = NBTConverter.NBTtoJSON_Compound(payload.getCompoundTag("data"), new JsonObject());
		
		readFromJson(JsonHelper.GetObject(base, "line"), EnumSaveType.CONFIG);
	}
	
	@Override
	public JsonObject writeToJson(JsonObject json, EnumSaveType saveType)
	{
		if(saveType != EnumSaveType.CONFIG)
		{
			return json;
		}
		
		json.add("properties", info.writeToJson(new JsonObject(), saveType));
		
		JsonArray jArr = new JsonArray();
		
		for(Entry<Integer,IQuestLineEntry> entry : questList.entrySet())
		{
			JsonObject qle = entry.getValue().writeToJson(new JsonObject(), saveType);
			qle.addProperty("id", entry.getKey());
			jArr.add(qle);
		}
		
		json.add("quests", jArr);
		return json;
	}
	
	@Override
	public void readFromJson(JsonObject json, EnumSaveType saveType)
	{
		if(saveType != EnumSaveType.CONFIG)
		{
			return;
		}
		
		info.readFromJson(JsonHelper.GetObject(json, "properties"), saveType);
		
		questList.clear();
		for(JsonElement entry : JsonHelper.GetArray(json, "quests"))
		{
			if(entry == null)
			{
				continue;
			}
			
			if(entry.isJsonPrimitive() && entry.getAsJsonPrimitive().isNumber()) // Backwards compatibility
			{
				questList.put(entry.getAsInt(), new QuestLineEntry(0, 0));
			} else if(entry.isJsonObject())
			{
				JsonObject jl = entry.getAsJsonObject();
				int id = JsonHelper.GetNumber(jl, "id", -1).intValue();
				
				if(id >= 0)
				{
					questList.put(id, new QuestLineEntry(jl));
				}
			}
		}
		
		this.setupProps();
	}
}
