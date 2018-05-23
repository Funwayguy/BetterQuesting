package betterquesting.questing;

import betterquesting.api2.storage.DBEntry;
import betterquesting.api2.storage.SimpleDatabase;
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

public class QuestLine extends SimpleDatabase<IQuestLineEntry> implements IQuestLine
{
	private IPropertyContainer info = new PropertyContainer();
	
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
	public IQuestLineEntry createNewEntry(int id)
	{
		return add(id, new QuestLineEntry(0, 0, 24)).getValue();
	}
	
	@Override
	public IPropertyContainer getProperties()
	{
		return info;
	}
	
	@Override
	public int getQuestAt(int x, int y)
	{
		for(DBEntry<IQuestLineEntry> entry : getEntries())
		{
			int i1 = entry.getValue().getPosX();
			int j1 = entry.getValue().getPosY();
			int i2 = i1 + entry.getValue().getSize();
			int j2 = j1 + entry.getValue().getSize();
			
			if(x >= i1 && x < i2 && y >= j1 && y < j2)
			{
				return entry.getID();
			}
		}
		
		return -1;
	}
	
	@Override
	public QuestingPacket getSyncPacket()
	{
		NBTTagCompound tags = new NBTTagCompound();
		NBTTagCompound base = new NBTTagCompound();
		base.setTag("line", writeToNBT(new NBTTagCompound(), EnumSaveType.CONFIG));
		tags.setTag("data", base);
		tags.setInteger("lineID", parentDB.getID(this));
		
		return new QuestingPacket(PacketTypeNative.LINE_SYNC.GetLocation(), tags);
	}
	
	@Override
	public void readPacket(NBTTagCompound payload)
	{
		readFromNBT(payload.getCompoundTag("data").getCompoundTag("line"), EnumSaveType.CONFIG);
	}
	
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound json, EnumSaveType saveType)
	{
		if(saveType != EnumSaveType.CONFIG)
		{
			return json;
		}
		
		json.setTag("properties", info.writeToNBT(new NBTTagCompound()));
		
		NBTTagList jArr = new NBTTagList();
		
		for(DBEntry<IQuestLineEntry> entry : getEntries())
		{
			NBTTagCompound qle = entry.getValue().writeToNBT(new NBTTagCompound(), saveType);
			qle.setInteger("id", entry.getID());
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
		
		info.readFromNBT(json.getCompoundTag("properties"));
		
		reset();
		
		NBTTagList qList = json.getTagList("quests", 10);
		for(int i = 0; i < qList.tagCount(); i++)
		{
			NBTBase entry = qList.get(i);
			
			if(entry.getId() != 10)
			{
				continue;
			}
			
			NBTTagCompound qTag = (NBTTagCompound)entry;
			
			int id = qTag.hasKey("id", 99) ? qTag.getInteger("id") : -1;
			
			if(id >= 0)
			{
				add(id, new QuestLineEntry(qTag));
			}
		}
		
		this.setupProps();
	}
}
