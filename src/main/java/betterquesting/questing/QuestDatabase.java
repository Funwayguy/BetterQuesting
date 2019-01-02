package betterquesting.questing;

import betterquesting.api.network.QuestingPacket;
import betterquesting.api.questing.IQuest;
import betterquesting.api.questing.IQuestDatabase;
import betterquesting.api2.storage.BigDatabase;
import betterquesting.api2.storage.DBEntry;
import betterquesting.network.PacketTypeNative;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

import java.util.List;
import java.util.UUID;

public final class QuestDatabase extends BigDatabase<IQuest> implements IQuestDatabase
{
	public static final QuestDatabase INSTANCE = new QuestDatabase();
	
	public QuestDatabase()
    {
        super(20);
    }
	
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
		NBTTagCompound tags = new NBTTagCompound();
		NBTTagCompound base = new NBTTagCompound();
		base.setTag("config", writeToNBT(new NBTTagList(), null));
		base.setTag("progress", writeProgressToNBT(new NBTTagList(), null));
		tags.setTag("data", base);
		return new QuestingPacket(PacketTypeNative.QUEST_DATABASE.GetLocation(), tags);
	}
	
	@Override
	public void readPacket(NBTTagCompound payload)
	{
		NBTTagCompound base = payload.getCompoundTag("data");
		
		readFromNBT(base.getTagList("config", 10), false);
		readProgressFromNBT(base.getTagList("progress", 10), false);
	}
	
	@Override
	public NBTTagList writeToNBT(NBTTagList json, List<UUID> users)
	{
		for(DBEntry<IQuest> entry : this.getEntries())
		{
			NBTTagCompound jq = new NBTTagCompound();
			entry.getValue().writeToNBT(jq);
			jq.setInteger("questID", entry.getID());
			json.appendTag(jq);
		}
		
		return json;
	}
	
	@Override
	public void readFromNBT(NBTTagList nbt, boolean merge)
	{
		this.reset();
		
		for(int i = 0; i < nbt.tagCount(); i++)
		{
			NBTBase entry = nbt.get(i);
			
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
			quest.readFromNBT(qTag);
		}
	}
	
	@Override
	public NBTTagList writeProgressToNBT(NBTTagList json, List<UUID> users)
	{
		for(DBEntry<IQuest> entry : this.getEntries())
		{
			NBTTagCompound jq = new NBTTagCompound();
			entry.getValue().writeProgressToNBT(jq, users);
			jq.setInteger("questID", entry.getID());
			json.appendTag(jq);
		}
		
		return json;
	}
	
	@Override
	public void readProgressFromNBT(NBTTagList json, boolean merge)
	{
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
			
			if(quest != null)
			{
				quest.readProgressFromNBT(qTag, merge);
			}
		}
	}
}
