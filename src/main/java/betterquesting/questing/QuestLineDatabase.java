package betterquesting.questing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import betterquesting.api2.storage.DBEntry;
import betterquesting.api2.storage.SimpleDatabase;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import betterquesting.api.enums.EnumSaveType;
import betterquesting.api.network.QuestingPacket;
import betterquesting.api.questing.IQuestLine;
import betterquesting.api.questing.IQuestLineDatabase;
import betterquesting.network.PacketTypeNative;

public final class QuestLineDatabase extends SimpleDatabase<IQuestLine> implements IQuestLineDatabase
{
	public static final QuestLineDatabase INSTANCE = new QuestLineDatabase();
	
	// TODO: Probably should make this thread safe
	private final List<Integer> lineOrder = new ArrayList<>();
	
	@Override
	public int getOrderIndex(int lineID)
	{
		if(getValue(lineID) == null)
		{
			return -1;
		} else if(!lineOrder.contains(lineID))
		{
			lineOrder.add(lineID);
		}
		
		return lineOrder.indexOf(lineID);
	}
	
	@Override
	public void setOrderIndex(int lineID, int index)
	{
		lineOrder.remove((Integer)lineID);
		lineOrder.add(index, lineID);
	}
	
	@Override
	public IQuestLine createNew(int id)
	{
		IQuestLine ql = new QuestLine();
		ql.setParentDatabase(this);
		this.add(id, ql);
		return ql;
	}
	
	@Override
	public void removeQuest(int questID)
	{
		for(DBEntry<IQuestLine> ql : getEntries())
		{
			ql.getValue().removeID(questID);
		}
	}
	
	@Override
	public QuestingPacket getSyncPacket()
	{
		NBTTagCompound tags = new NBTTagCompound();
		tags.setTag("data", writeToNBT(new NBTTagList(), EnumSaveType.CONFIG));
		return new QuestingPacket(PacketTypeNative.LINE_DATABASE.GetLocation(), tags);
	}
	
	@Override
	public void readPacket(NBTTagCompound payload)
	{
		this.readFromNBT(payload.getTagList("data", 10), EnumSaveType.CONFIG);
	}
	
	@Override
	public NBTTagList writeToNBT(NBTTagList json, EnumSaveType saveType)
	{
		if(saveType != EnumSaveType.CONFIG)
		{
			return json;
		}
		
		for(DBEntry<IQuestLine> entry : getEntries())
		{
			NBTTagCompound jObj = entry.getValue().writeToNBT(new NBTTagCompound(), saveType);
			jObj.setInteger("lineID", entry.getID());
			jObj.setInteger("order", getOrderIndex(entry.getID()));
			json.appendTag(jObj);
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
		
		reset();
		
		List<IQuestLine> unassigned = new ArrayList<>();
		
		HashMap<Integer,Integer> orderMap = new HashMap<>();
		
		for(int i = 0; i < json.tagCount(); i++)
		{
			NBTBase entry = json.get(i);
			
			if(entry.getId() != 10)
			{
				continue;
			}
			
			NBTTagCompound jql = (NBTTagCompound)entry;
			
			int id = jql.hasKey("lineID", 99) ? jql.getInteger("lineID") : -1;
			int order = jql.hasKey("order", 99) ? jql.getInteger("order") : -1;
			
			QuestLine line = new QuestLine();
			line.readFromNBT(jql, saveType);
			
			if(id >= 0)
			{
				add(id, line);
			} else
			{
				unassigned.add(line);
			}
			
			if(order >= 0)
			{
				orderMap.put(order, id);
			}
		}
		
		// Legacy support ONLY
		for(IQuestLine q : unassigned)
		{
			add(nextID(), q);
		}
		
		List<Integer> orderKeys = new ArrayList<>(orderMap.keySet());
		Collections.sort(orderKeys);
		
		lineOrder.clear();
		
		for(int o : orderKeys)
		{
			lineOrder.add(orderMap.get(o));
		}
	}
}
