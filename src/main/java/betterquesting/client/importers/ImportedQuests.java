package betterquesting.client.importers;

import betterquesting.api.questing.IQuest;
import betterquesting.api.questing.IQuestDatabase;
import betterquesting.api2.storage.DBEntry;
import betterquesting.api2.storage.SimpleDatabase;
import betterquesting.questing.QuestInstance;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class ImportedQuests extends SimpleDatabase<IQuest> implements IQuestDatabase
{
	@Override
	public IQuest createNew(int id)
	{
		return this.add(id, new QuestInstance()).getValue();
	}
    
    @Override
    public List<DBEntry<IQuest>> bulkLookup(int... ids)
    {
        if(ids == null || ids.length <= 0) return Collections.emptyList();
        
        List<DBEntry<IQuest>> values = new ArrayList<>();
        
        synchronized(this)
        {
            for(int i : ids)
            {
                IQuest v = getValue(i);
                if(v != null) values.add(new DBEntry<>(i, v));
            }
        }
        
        return values;
    }
	
	@Override
	public ListNBT writeToNBT(ListNBT nbt, List<Integer> subset)
	{
		for(DBEntry<IQuest> entry : this.getEntries())
		{
		    if(subset != null && !subset.contains(entry.getID())) continue;
			CompoundNBT jq = new CompoundNBT();
			entry.getValue().writeToNBT(jq);
			jq.putInt("questID", entry.getID());
			nbt.add(jq);
		}
		
		return nbt;
	}
	
	@Override
	public void readFromNBT(ListNBT nbt, boolean merge)
	{
		this.reset();
		
		for(int i = 0; i < nbt.size(); i++)
		{
			CompoundNBT qTag = nbt.getCompound(i);
			
			int qID = qTag.contains("questID", 99) ? qTag.getInt("questID") : -1;
			if(qID < 0) continue;
			
			IQuest quest = getValue(qID);
			quest = quest != null? quest : this.createNew(qID);
			quest.readFromNBT(qTag);
		}
	}
	
	@Override
    public ListNBT writeProgressToNBT(ListNBT nbt, @Nullable List<UUID> users)
    {
        return nbt;
    }
    
    @Override
    public void readProgressFromNBT(ListNBT nbt, boolean merge)
    {
    }
}
