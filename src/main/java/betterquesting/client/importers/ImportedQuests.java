package betterquesting.client.importers;

import betterquesting.api.network.QuestingPacket;
import betterquesting.api.questing.IQuestDatabase;
import betterquesting.api2.storage.BigDatabase;
import betterquesting.api2.storage.DBEntry;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import betterquesting.api.enums.EnumSaveType;
import betterquesting.api.questing.IQuest;
import betterquesting.questing.QuestInstance;

public class ImportedQuests extends BigDatabase<IQuest> implements IQuestDatabase
{
	@Override
	public IQuest createNew(int id)
	{
		IQuest q = new QuestInstance();
		q.setParentDatabase(this);
		this.add(id, q);
		return q;
	}
	
	@Override
	public QuestingPacket getSyncPacket()
	{
		return null;
	}
	
	@Override
	public void readPacket(NBTTagCompound payload)
	{
	}
	
	@Override
	public NBTTagList writeToNBT(NBTTagList json, EnumSaveType saveType)
	{
		if(saveType != EnumSaveType.CONFIG)
		{
			return json;
		}
		
		for(DBEntry<IQuest> entry : this.getEntries())
		{
			NBTTagCompound jq = new NBTTagCompound();
			entry.getValue().writeToNBT(jq, saveType);
			jq.setInteger("questID", entry.getID());
			json.appendTag(jq);
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
		
		this.reset();
		
		for(int i = 0; i < json.tagCount(); i++)
		{
			NBTBase entry = json.get(i);
			
			if(entry.getId() != 10)
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
			quest = quest != null? quest : this.createNew(qID);
			quest.readFromNBT(qTag, EnumSaveType.CONFIG);
		}
	}
}
