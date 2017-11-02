package betterquesting.questing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import betterquesting.api.api.ApiReference;
import betterquesting.api.api.QuestingAPI;
import betterquesting.api.enums.EnumSaveType;
import betterquesting.api.network.QuestingPacket;
import betterquesting.api.properties.IPropertyContainer;
import betterquesting.api.properties.IPropertyType;
import betterquesting.api.properties.NativeProps;
import betterquesting.api.questing.IQuestLine;
import betterquesting.api.questing.IQuestLineDatabase;
import betterquesting.api.questing.IQuestLineEntry;
import betterquesting.network.PacketTypeNative;
import betterquesting.storage.PropertyContainer;

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
		tags.setTag("data", writeToNBT(new NBTTagCompound(), EnumSaveType.CONFIG));
		tags.setInteger("lineID", parentDB.getKey(this));
		
		return new QuestingPacket(PacketTypeNative.LINE_SYNC.GetLocation(), tags);
	}
	
	@Override
	public void readPacket(NBTTagCompound payload)
	{
		readFromNBT(payload.getCompoundTag("data"), EnumSaveType.CONFIG);
	}
	
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound json, EnumSaveType saveType)
	{
		if(saveType != EnumSaveType.CONFIG)
		{
			return json;
		}
		
		json.setTag("properties", info.writeToNBT(new NBTTagCompound(), saveType));
		
		NBTTagList jArr = new NBTTagList();
		
		for(Entry<Integer,IQuestLineEntry> entry : questList.entrySet())
		{
			NBTTagCompound qle = entry.getValue().writeToNBT(new NBTTagCompound(), saveType);
			qle.setInteger("id", entry.getKey());
			jArr.appendTag(qle);
		}
		
		json.setTag("quests", jArr);
		return json;
	}
	
	@Override
	public void readFromNBT(NBTTagCompound json, EnumSaveType saveType)
	{
		if(saveType != EnumSaveType.CONFIG)
		{
			return;
		}
		
		info.readFromNBT(json.getCompoundTag("properties"), saveType);
		
		questList.clear();
		NBTTagList qList = json.getTagList("quests", 10);
		for(int i = 0; i < qList.tagCount(); i++)
		{
			NBTBase entry = qList.get(i);
			
			if(entry == null || entry.getId() != 10)
			{
				continue;
			}
			
			NBTTagCompound qTag = (NBTTagCompound)entry;
			
			int id = qTag.hasKey("id", 99) ? qTag.getInteger("id") : -1;
			
			if(id >= 0)
			{
				questList.put(id, new QuestLineEntry(qTag));
			}
		}
		
		this.setupProps();
	}
}
