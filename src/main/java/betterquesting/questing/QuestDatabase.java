package betterquesting.questing;

import betterquesting.api.network.QuestingPacket;
import betterquesting.api.questing.IQuest;
import betterquesting.api.questing.IQuestDatabase;
import betterquesting.api2.storage.DBEntry;
import betterquesting.api2.storage.SimpleDatabase;
import betterquesting.network.PacketTypeNative;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public final class QuestDatabase extends SimpleDatabase<IQuest> implements IQuestDatabase
{
	public static final QuestDatabase INSTANCE = new QuestDatabase();
	
	@Override
	public IQuest createNew(int id)
	{
		return this.add(id, new QuestInstance()).getValue();
	}
    
    @Override
    public synchronized List<DBEntry<IQuest>> bulkLookup(int... ids)
    {
        if(ids == null || ids.length <= 0) return Collections.emptyList();
        
        List<DBEntry<IQuest>> values = new ArrayList<>();
        
        for(int i : ids)
        {
            IQuest v = getValue(i);
            if(v != null) values.add(new DBEntry<>(i, v));
        }
        
        return values;
    }
    
    @Override
    public synchronized boolean removeID(int id)
    {
        boolean success = super.removeID(id);
        if(success) for(DBEntry<IQuest> entry : getEntries()) removeReq(entry.getValue(), id);
        return success;
    }
    
    @Override
    public synchronized boolean removeValue(IQuest value)
    {
        int id = this.getID(value);
        if(id < 0) return false;
        boolean success = this.removeValue(value);
        if(success) for(DBEntry<IQuest> entry : getEntries()) removeReq(entry.getValue(), id);
        return success;
    }
    
    private void removeReq(IQuest quest, int id)
    {
        int[] orig = quest.getRequirements();
        if(orig.length <= 0) return;
        boolean hasRemoved = false;
        int[] rem = new int[orig.length - 1];
        for(int i = 0; i < orig.length; i++)
        {
            if(!hasRemoved && orig[i] == id)
            {
                hasRemoved = true;
                continue;
            } else if(!hasRemoved && i >= rem.length) break;
            
            rem[!hasRemoved ? i : (i - 1)] = orig[i];
        }
        
        if(hasRemoved) quest.setRequirements(rem);
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
	public synchronized NBTTagList writeToNBT(NBTTagList json, List<UUID> users)
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
	public synchronized void readFromNBT(NBTTagList nbt, boolean merge)
	{
		this.reset();
		
		for(int i = 0; i < nbt.tagCount(); i++)
		{
			NBTTagCompound qTag = nbt.getCompoundTagAt(i);
			
			int qID = qTag.hasKey("questID", 99) ? qTag.getInteger("questID") : -1;
			if(qID < 0) continue;
			
			IQuest quest = getValue(qID);
			quest = quest != null? quest : this.createNew(qID);
			quest.readFromNBT(qTag);
		}
	}
	
	@Override
	public synchronized NBTTagList writeProgressToNBT(NBTTagList json, List<UUID> users)
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
	public synchronized void readProgressFromNBT(NBTTagList json, boolean merge)
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
