package betterquesting.questing;

import betterquesting.api.api.QuestingAPI;
import betterquesting.api.enums.EnumLogic;
import betterquesting.api.enums.EnumQuestState;
import betterquesting.api.enums.EnumQuestVisibility;
import betterquesting.api.properties.IPropertyType;
import betterquesting.api.properties.NativeProps;
import betterquesting.api.questing.IQuest;
import betterquesting.api.questing.rewards.IReward;
import betterquesting.api.questing.tasks.ITask;
import betterquesting.api.utils.BigItemStack;
import betterquesting.api2.cache.CapabilityProviderQuestCache;
import betterquesting.api2.cache.QuestCache;
import betterquesting.api2.storage.DBEntry;
import betterquesting.api2.storage.IDatabaseNBT;
import betterquesting.api2.utils.ParticipantInfo;
import betterquesting.core.BetterQuesting;
import betterquesting.questing.rewards.RewardStorage;
import betterquesting.questing.tasks.TaskStorage;
import betterquesting.storage.PropertyContainer;
import betterquesting.storage.QuestSettings;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.nbt.*;
import net.minecraftforge.common.util.LazyOptional;
import org.apache.logging.log4j.Level;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;

public class QuestInstance implements IQuest
{
	private final TaskStorage tasks = new TaskStorage();
	private final RewardStorage rewards = new RewardStorage();
	
	private final HashMap<UUID, CompoundNBT> completeUsers = new HashMap<>();
    private int[] preRequisites = new int[0];
	
	private final PropertyContainer qInfo = new PropertyContainer();
	
	public QuestInstance()
	{
		this.setupProps();
	}
	
	private void setupProps()
	{
		setupValue(NativeProps.NAME, "New Quest");
		setupValue(NativeProps.DESC, "No Description");
		
		setupValue(NativeProps.ICON, new BigItemStack(Items.NETHER_STAR));
		
		setupValue(NativeProps.SOUND_COMPLETE);
		setupValue(NativeProps.SOUND_UPDATE);
		//setupValue(NativeProps.SOUND_UNLOCK);
		
		setupValue(NativeProps.LOGIC_QUEST, EnumLogic.AND);
		setupValue(NativeProps.LOGIC_TASK, EnumLogic.AND);
		
		setupValue(NativeProps.REPEAT_TIME, -1);
		setupValue(NativeProps.REPEAT_REL, true);
		setupValue(NativeProps.LOCKED_PROGRESS, false);
		setupValue(NativeProps.AUTO_CLAIM, false);
		setupValue(NativeProps.SILENT, false);
		setupValue(NativeProps.MAIN, false);
		setupValue(NativeProps.GLOBAL_SHARE, false);
		setupValue(NativeProps.SIMULTANEOUS, false);
		setupValue(NativeProps.VISIBILITY, EnumQuestVisibility.NORMAL);
	}
	
	private <T> void setupValue(IPropertyType<T> prop)
	{
		this.setupValue(prop, prop.getDefault());
	}
	
	private <T> void setupValue(IPropertyType<T> prop, T def)
	{
		qInfo.setProperty(prop, qInfo.getProperty(prop, def));
	}
	
	@Override
	public void update(PlayerEntity player)
	{
		UUID playerID = QuestingAPI.getQuestingUUID(player);
		
        int done = 0;
        
        for(DBEntry<ITask> entry : tasks.getEntries())
        {
            if(entry.getValue().isComplete(playerID))
            {
                done++;
            }
        }
        
        if(tasks.size() <= 0 || qInfo.getProperty(NativeProps.LOGIC_TASK).getResult(done, tasks.size()))
        {
            setComplete(playerID, System.currentTimeMillis());
        } else if(done > 0 && qInfo.getProperty(NativeProps.SIMULTANEOUS)) // TODO: There is actually an exploit here to do with locked progression bypassing simultaneous reset conditions. Fix?
        {
            resetUser(playerID, false);
        }
	}
	
	/**
	 * Fired when someone clicks the detect button for this quest
	 */
	@Override
	public void detect(PlayerEntity player)
	{
		UUID playerID = QuestingAPI.getQuestingUUID(player);
        LazyOptional<QuestCache> qc = player.getCapability(CapabilityProviderQuestCache.CAP_QUEST_CACHE, null);
        if(!qc.isPresent()) return;
        int questID = QuestDatabase.INSTANCE.getID(this);
		
		if(isComplete(playerID) && (qInfo.getProperty(NativeProps.REPEAT_TIME) < 0 || rewards.size() <= 0))
		{
			return;
		} else if(!canSubmit(player))
		{
			return;
		}
		
		if(isUnlocked(playerID) || QuestSettings.INSTANCE.getProperty(NativeProps.EDIT_MODE))
		{
			int done = 0;
			boolean update = false;
            
            ParticipantInfo partInfo = new ParticipantInfo(player);
			
			for(DBEntry<ITask> entry : tasks.getEntries())
			{
				if(!entry.getValue().isComplete(playerID))
				{
					entry.getValue().detect(partInfo, new DBEntry<>(questID, this));
					
					if(entry.getValue().isComplete(playerID))
					{
						done++;
						update = true;
					}
				} else
				{
					done++;
				}
			}
			// Note: Tasks can mark the quest dirty themselves if progress changed but hasn't fully completed.
			if(tasks.size() <= 0 || qInfo.getProperty(NativeProps.LOGIC_TASK).getResult(done, tasks.size()))
			{
			    // State won't be auto updated in edit mode so we force change it here and mark it for re-sync
				if(QuestSettings.INSTANCE.getProperty(NativeProps.EDIT_MODE)) setComplete(playerID, System.currentTimeMillis());
				qc.ifPresent((q) -> q.markQuestDirty(questID));
			} else if(update && qInfo.getProperty(NativeProps.SIMULTANEOUS))
			{
				resetUser(playerID, false);
				qc.ifPresent((q) -> q.markQuestDirty(questID));
			} else if(update)
			{
				qc.ifPresent((q) -> q.markQuestDirty(questID));
			}
		}
	}
	
	@Override
	public boolean hasClaimed(UUID uuid)
	{
		if(rewards.size() <= 0) return true;
  
		synchronized(completeUsers)
        {
            if(qInfo.getProperty(NativeProps.GLOBAL) && !qInfo.getProperty(NativeProps.GLOBAL_SHARE))
            {
                // TODO: Figure out some replacement to track participation
                for(CompoundNBT entry : completeUsers.values())
                {
                    if(entry.getBoolean("claimed")) return true;
                }
                
                return false;
            }
    
            CompoundNBT entry = getCompletionInfo(uuid);
            return entry != null && entry.getBoolean("claimed");
        }
	}
	
	@Override
	public boolean canClaim(PlayerEntity player)
	{
	    UUID pID = QuestingAPI.getQuestingUUID(player);
		CompoundNBT entry = getCompletionInfo(pID);
		
		if(entry == null || hasClaimed(pID) || canSubmit(player))
		{
			return false;
		} else
		{
		    int questID = QuestDatabase.INSTANCE.getID(this);
			for(DBEntry<IReward> rew : rewards.getEntries())
			{
				if(!rew.getValue().canClaim(player, new DBEntry<>(questID, this)))
				{
					return false;
				}
			}
		}
		
		return true;
	}
	
	@Override
	public void claimReward(PlayerEntity player)
	{
        int questID = QuestDatabase.INSTANCE.getID(this);
		for(DBEntry<IReward> rew : rewards.getEntries())
		{
			rew.getValue().claimReward(player, new DBEntry<>(questID, this));
		}
		
		UUID pID = QuestingAPI.getQuestingUUID(player);
		
        synchronized(completeUsers)
        {
            CompoundNBT entry = getCompletionInfo(pID);

            if(entry == null)
            {
                entry = new CompoundNBT();
                this.completeUsers.put(pID, entry);
            }
            
            entry.putBoolean("claimed", true);
            entry.putLong("timestamp", System.currentTimeMillis());
        }
		
        player.getCapability(CapabilityProviderQuestCache.CAP_QUEST_CACHE, null).ifPresent((q) -> q.markQuestDirty(questID));
	}
	
	@Override
	public boolean canSubmit(@Nonnull PlayerEntity player)
	{
		UUID playerID = QuestingAPI.getQuestingUUID(player);
		
		synchronized(completeUsers)
        {
            CompoundNBT entry = this.getCompletionInfo(playerID);
            if(entry == null) return true;
            
            if(!entry.getBoolean("claimed") && getProperty(NativeProps.REPEAT_TIME) >= 0) // Complete but repeatable
            {
                if(tasks.size() <= 0) return true;
        
                int done = 0;
        
                for(DBEntry<ITask> tsk : tasks.getEntries())
                {
                    if(tsk.getValue().isComplete(playerID))
                    {
                        done += 1;
                    }
                }
        
                return !qInfo.getProperty(NativeProps.LOGIC_TASK).getResult(done, tasks.size());
            } else
            {
                return false;
            }
        }
	}
	
	@Override
	public boolean isUnlocked(UUID uuid)
	{
		if(preRequisites.length <= 0) return true;
		
		int A = 0;
		int B = preRequisites.length;
		
		for(DBEntry<IQuest> quest : QuestDatabase.INSTANCE.bulkLookup(getRequirements()))
		{
			if(quest.getValue().isComplete(uuid))
			{
				A++;
			}
		}
		
		return qInfo.getProperty(NativeProps.LOGIC_QUEST).getResult(A, B);
	}
	
	@Override
	public void setComplete(UUID uuid, long timestamp)
    {
        if(uuid == null) return;
        
        synchronized(completeUsers)
        {
            CompoundNBT entry = this.getCompletionInfo(uuid);
    
            if(entry == null)
            {
                entry = new CompoundNBT();
                completeUsers.put(uuid, entry);
            }
    
            entry.putBoolean("claimed", false);
            entry.putLong("timestamp", timestamp);
        }
    }
    
	/**
	 * Returns true if the quest has been completed at least once
	 */
	@Override
	public boolean isComplete(UUID uuid)
	{
		if(qInfo.getProperty(NativeProps.GLOBAL))
		{
			return completeUsers.size() > 0;
		} else
		{
			return getCompletionInfo(uuid) != null;
		}
	}
	
	@Override
	public EnumQuestState getState(UUID uuid)
	{
		if(this.isComplete(uuid))
		{
			if(this.hasClaimed(uuid))
			{
				return EnumQuestState.COMPLETED;
			} else
			{
				return EnumQuestState.UNCLAIMED;
			}
		} else if(this.isUnlocked(uuid))
		{
			return EnumQuestState.UNLOCKED;
		}
		
		return EnumQuestState.LOCKED;
	}
	
	@Override
	public CompoundNBT getCompletionInfo(UUID uuid)
	{
	    synchronized(completeUsers)
        {
            return completeUsers.get(uuid);
        }
	}
	
	@Override
    public void setCompletionInfo(UUID uuid, CompoundNBT nbt)
    {
        if(uuid == null) return;
        
        synchronized(completeUsers)
        {
            if(nbt == null)
            {
                completeUsers.remove(uuid);
            } else
            {
                completeUsers.put(uuid, nbt);
            }
        }
    }
	
	/**
	 * Resets task progress and claim status. If performing a full reset, completion status will also be erased
	 */
	@Override
	public void resetUser(@Nullable UUID uuid, boolean fullReset)
	{
	    synchronized(completeUsers)
        {
            if(fullReset)
            {
                if(uuid == null)
                {
                    completeUsers.clear();
                } else
                {
                    completeUsers.remove(uuid);
                }
            } else
            {
                if(uuid == null)
                {
                    completeUsers.forEach((key, value) -> {
                        value.putBoolean("claimed", false);
                        value.putLong("timestamp", 0);
                    });
                } else
                {
                    CompoundNBT entry = getCompletionInfo(uuid);
                    if(entry != null)
                    {
                        entry.putBoolean("claimed", false);
                        entry.putLong("timestamp", 0);
                    }
                }
            }
    
            tasks.getEntries().forEach((value) -> value.getValue().resetUser(uuid));
        }
	}
	
	@Override
	public IDatabaseNBT<ITask, ListNBT, ListNBT> getTasks()
	{
		return tasks;
	}
	
	@Override
	public IDatabaseNBT<IReward, ListNBT, ListNBT> getRewards()
	{
		return rewards;
	}
	
	@Nonnull
	@Override
    public int[] getRequirements()
    {
        return this.preRequisites;
    }
    
    public void setRequirements(@Nonnull int[] req)
    {
        this.preRequisites = req;
    }
	
	@Override
	public CompoundNBT writeToNBT(CompoundNBT jObj)
	{
		jObj.put("properties", qInfo.writeToNBT(new CompoundNBT()));
		jObj.put("tasks", tasks.writeToNBT(new ListNBT(), null));
		jObj.put("rewards", rewards.writeToNBT(new ListNBT(), null));
		jObj.put("preRequisites", new IntArrayNBT(getRequirements()));
		
		return jObj;
	}
	
	@Override
	public void readFromNBT(CompoundNBT jObj)
	{
		this.qInfo.readFromNBT(jObj.getCompound("properties"));
		this.tasks.readFromNBT(jObj.getList("tasks", 10), false);
		this.rewards.readFromNBT(jObj.getList("rewards", 10), false);
		
		if(jObj.getTagId("preRequisites") == 11) // Native NBT
		{
		    setRequirements(jObj.getIntArray("preRequisites"));
		} else // Probably an NBTTagList
		{
			ListNBT rList = jObj.getList("preRequisites", 4);
			int[] req = new int[rList.size()];
			for(int i = 0; i < rList.size(); i++)
			{
				INBT pTag = rList.get(i);
				req[i] = pTag instanceof NumberNBT ? ((NumberNBT)pTag).getInt() : -1;
			}
			setRequirements(req);
		}
		
		this.setupProps();
	}
	
	@Override
	public CompoundNBT writeProgressToNBT(CompoundNBT json, @Nullable List<UUID> users)
	{
	    synchronized(completeUsers)
        {
            ListNBT comJson = new ListNBT();
            for(Entry<UUID, CompoundNBT> entry : completeUsers.entrySet())
            {
                if(users != null && !users.contains(entry.getKey())) continue;
                CompoundNBT tags = entry.getValue().copy();
                tags.putString("uuid", entry.getKey().toString());
                comJson.add(tags);
            }
            json.put("completed", comJson);
    
            ListNBT tskJson = tasks.writeProgressToNBT(new ListNBT(), users);
            json.put("tasks", tskJson);
    
            return json;
        }
	}
	
	@Override
	public void readProgressFromNBT(CompoundNBT json, boolean merge)
	{
	    synchronized(completeUsers)
        {
            if(!merge) completeUsers.clear();
            ListNBT comList = json.getList("completed", 10);
            for(int i = 0; i < comList.size(); i++)
            {
                CompoundNBT entry = comList.getCompound(i).copy();
                
                try
                {
                    UUID uuid = UUID.fromString(entry.getString("uuid"));
                    completeUsers.put(uuid, entry);
                } catch(Exception e)
                {
                    BetterQuesting.logger.log(Level.ERROR, "Unable to load UUID for quest", e);
                }
            }
    
            tasks.readProgressFromNBT(json.getList("tasks", 10), merge);
        }
	}
	
    @Override
	public void setClaimed(UUID uuid, long timestamp)
	{
		synchronized(completeUsers)
        {
            CompoundNBT entry = this.getCompletionInfo(uuid);
    
            if(entry != null)
            {
                entry.putBoolean("claimed", true);
                entry.putLong("timestamp", timestamp);
            } else
            {
                entry = new CompoundNBT();
                entry.putBoolean("claimed", true);
                entry.putLong("timestamp", timestamp);
                completeUsers.put(uuid, entry);
            }
        }
	}
    
    @Override
    public <T> T getProperty(IPropertyType<T> prop)
    {
        return qInfo.getProperty(prop);
    }
    
    @Override
    public <T> T getProperty(IPropertyType<T> prop, T def)
    {
        return qInfo.getProperty(prop, def);
    }
    
    @Override
    public boolean hasProperty(IPropertyType<?> prop)
    {
        return qInfo.hasProperty(prop);
    }
    
    @Override
    public <T> void setProperty(IPropertyType<T> prop, T value)
    {
        qInfo.setProperty(prop, value);
    }
    
    @Override
    public void removeProperty(IPropertyType<?> prop)
    {
        qInfo.removeProperty(prop);
    }
    
    @Override
    public void removeAllProps()
    {
        qInfo.removeAllProps();
    }
}
