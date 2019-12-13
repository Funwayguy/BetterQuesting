package betterquesting.questing;

import betterquesting.api.enums.EnumQuestVisibility;
import betterquesting.api.properties.IPropertyType;
import betterquesting.api.properties.NativeProps;
import betterquesting.api.questing.IQuestLine;
import betterquesting.api.questing.IQuestLineEntry;
import betterquesting.api.utils.BigItemStack;
import betterquesting.api2.storage.DBEntry;
import betterquesting.api2.storage.SimpleDatabase;
import betterquesting.storage.PropertyContainer;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;

import javax.annotation.Nullable;
import java.util.List;

public class QuestLine extends SimpleDatabase<IQuestLineEntry> implements IQuestLine
{
	private PropertyContainer info = new PropertyContainer();
	
	public QuestLine()
	{
		setupProps();
	}
	
	private void setupProps()
	{
		this.setupValue(NativeProps.NAME, "New Quest Line");
		this.setupValue(NativeProps.DESC, "No Description");
		this.setupValue(NativeProps.ICON, new BigItemStack(Items.BOOK));
		this.setupValue(NativeProps.VISIBILITY, EnumQuestVisibility.NORMAL);
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
    public IQuestLineEntry createNew(int id)
    {
        IQuestLineEntry qle = new QuestLineEntry(0, 0, 24, 24);
        this.add(id, qle);
        return qle;
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
	public DBEntry<IQuestLineEntry> getEntryAt(int x, int y)
	{
		for(DBEntry<IQuestLineEntry> entry : getEntries())
		{
			int i1 = entry.getValue().getPosX();
			int j1 = entry.getValue().getPosY();
			int i2 = i1 + entry.getValue().getSizeX();
			int j2 = j1 + entry.getValue().getSizeY();
			
			if(x >= i1 && x < i2 && y >= j1 && y < j2)
			{
				return entry;
			}
		}
		
		return null;
	}
	
	@Override
	public CompoundNBT writeToNBT(CompoundNBT json, @Nullable List<Integer> subset)
	{
		json.put("properties", info.writeToNBT(new CompoundNBT()));
		
		ListNBT jArr = new ListNBT();
		
		for(DBEntry<IQuestLineEntry> entry : getEntries())
		{
			CompoundNBT qle = entry.getValue().writeToNBT(new CompoundNBT());
			qle.putInt("id", entry.getID());
			jArr.add(qle);
		}
		
		json.put("quests", jArr);
		return json;
	}
	
	@Override
	public void readFromNBT(CompoundNBT json, boolean merge)
	{
		info.readFromNBT(json.getCompound("properties"));
		
		reset();
		
		ListNBT qList = json.getList("quests", 10);
		for(int i = 0; i < qList.size(); i++)
		{
			CompoundNBT qTag = qList.getCompound(i);
			
			int id = qTag.contains("id", 99) ? qTag.getInt("id") : -1;
			if(id< 0) continue;
			
			add(id, new QuestLineEntry(qTag));
		}
		
		this.setupProps();
	}
    
    @Override
    public <T> T getProperty(IPropertyType<T> prop)
    {
        return info.getProperty(prop);
    }
    
    @Override
    public <T> T getProperty(IPropertyType<T> prop, T def)
    {
        return info.getProperty(prop, def);
    }
    
    @Override
    public boolean hasProperty(IPropertyType<?> prop)
    {
        return info.hasProperty(prop);
    }
    
    @Override
    public <T> void setProperty(IPropertyType<T> prop, T value)
    {
        info.setProperty(prop, value);
    }
    
    @Override
    public void removeProperty(IPropertyType<?> prop)
    {
        info.removeProperty(prop);
    }
    
    @Override
    public void removeAllProps()
    {
        info.removeAllProps();
    }
}
