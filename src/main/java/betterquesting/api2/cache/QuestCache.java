package betterquesting.api2.cache;

import betterquesting.api.api.ApiReference;
import betterquesting.api.api.QuestingAPI;
import betterquesting.api.enums.EnumQuestVisibility;
import betterquesting.api.properties.NativeProps;
import betterquesting.api.questing.IQuest;
import betterquesting.api.storage.BQ_Settings;
import betterquesting.api2.storage.DBEntry;
import betterquesting.network.handlers.NetCacheSync;
import betterquesting.questing.QuestDatabase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.List;
import java.util.TreeSet;
import java.util.UUID;

public class QuestCache implements INBTSerializable<NBTTagCompound>
{
    // Quests that are visible to the player
    private final TreeSet<Integer> visibleQuests = new TreeSet<>();
    
    // Quests that are currently being undertaken. NOTE: Quests can be locked but still processing data if configured to do so
    private final TreeSet<Integer> activeQuests = new TreeSet<>();
    
    // Quests and their scheduled time of being reset
    private final TreeSet<QResetTime> resetSchedule = new TreeSet<>((o1, o2) -> o1.questID == o2.questID ? 0 : Long.compare(o1.time, o2.time));
    
    // Quests with pending auto claims (usually should be empty unless a condition needs to be met)
    private final TreeSet<Integer> autoClaims = new TreeSet<>();
    
    // Quests that need to be sent to the client to update progression (NOT for edits. Handle that elsewhere)
    private final TreeSet<Integer> markedDirty = new TreeSet<>();
    
    public synchronized int[] getActiveQuests()
    {
        // Probably a better way of doing this but this will do for now
        int i = 0;
        int[] aryAct = new int[activeQuests.size()];
        for(Integer q : activeQuests) aryAct[i++] = q;
        return aryAct;
    }
    
    public synchronized int[] getVisibleQuests()
    {
        // Probably a better way of doing this but this will do for now
        int i = 0;
        int[] aryVis = new int[visibleQuests.size()];
        for(Integer q : visibleQuests) aryVis[i++] = q;
        return aryVis;
    }
    
    public synchronized int[] getPendingAutoClaims()
    {
        // Probably a better way of doing this but this will do for now
        int i = 0;
        int[] aryAC = new int[autoClaims.size()];
        for(Integer q : autoClaims) aryAC[i++] = q;
        return aryAC;
    }
    
    public synchronized QResetTime[] getScheduledResets() // Already sorted by time
    {
        return resetSchedule.toArray(new QResetTime[0]);
    }
    
    public synchronized void markQuestDirty(int questID)
    {
        if(questID < 0) return;
        markedDirty.add(questID);
    }
    
    public synchronized void markQuestClean(int questID)
    {
        if(questID < 0) return;
        markedDirty.remove(questID);
    }
    
    public synchronized void cleanAllQuests()
    {
        markedDirty.clear();
    }
    
    public int[] getDirtyQuests()
    {
        // Probably a better way of doing this but this will do for now
        int i = 0;
        int[] aryMD = new int[markedDirty.size()];
        for(Integer q : markedDirty) aryMD[i++] = q;
        return aryMD;
    }
    
    // TODO: Ensure this is thread safe because we're likely going to run this in the background
    // NOTE: Only run this when the quests completion and claim states change. Use markQuestDirty() for progression changes that need syncing
    public synchronized void updateCache(EntityPlayer player)
    {
        if(player == null) return;
        
        UUID uuid = QuestingAPI.getQuestingUUID(player);
        List<DBEntry<IQuest>> questDB = QuestingAPI.getAPI(ApiReference.QUEST_DB).getEntries();
        
        NonNullList<Integer> tmpVisible = NonNullList.create();
        NonNullList<Integer> tmpActive = NonNullList.create();
        NonNullList<QResetTime> tmpReset = NonNullList.create();
        NonNullList<Integer> tmpAutoClaim = NonNullList.create();
        
        for(DBEntry<IQuest> entry : questDB)
        {
            if(entry.getValue().isUnlocked(uuid) || entry.getValue().isComplete(uuid) || entry.getValue().getProperty(NativeProps.LOCKED_PROGRESS)) // Unlocked or actively processing progression data
            {
                int repeat = entry.getValue().getProperty(NativeProps.REPEAT_TIME);
                NBTTagCompound ue = entry.getValue().getCompletionInfo(uuid);
                
                if((ue == null && entry.getValue().getTasks().size() <= 0) || entry.getValue().canSubmit(player)) // Can be active without completion in the case of locked progress. Also account for taskless quests
                {
                    tmpActive.add(entry.getID());
                } else if(ue != null) // These conditions only trigger after first completion
                {
                    if(repeat >= 0 && entry.getValue().hasClaimed(uuid))
                    {
                        long altTime = ue.getLong("timestamp");
                        if(repeat > 1 && !entry.getValue().getProperty(NativeProps.REPEAT_REL)) altTime -= (altTime % repeat);
                        tmpReset.add(new QResetTime(entry.getID(), altTime + (repeat * 50)));
                    }
                    
                    if(!entry.getValue().hasClaimed(uuid) && entry.getValue().getProperty(NativeProps.AUTO_CLAIM))
                    {
                        tmpAutoClaim.add(entry.getID());
                    }
                }
            }
            
            if(isQuestShown(entry.getValue(), uuid, player))
            {
                tmpVisible.add(entry.getID());
            }
        }
        
        visibleQuests.clear();
        visibleQuests.addAll(tmpVisible);
        
        activeQuests.clear();
        activeQuests.addAll(tmpActive);
        
        resetSchedule.clear();
        resetSchedule.addAll(tmpReset);
        
        autoClaims.clear();
        autoClaims.addAll(tmpAutoClaim);
        
        if(player instanceof EntityPlayerMP) NetCacheSync.sendSync((EntityPlayerMP)player);
    }
    
    @Override
    public synchronized NBTTagCompound serializeNBT()
    {
        NBTTagCompound tags = new NBTTagCompound();
        
        tags.setIntArray("visibleQuests", getVisibleQuests());
        tags.setIntArray("activeQuests", getActiveQuests());
        tags.setIntArray("autoClaims", getPendingAutoClaims());
        tags.setIntArray("markedDirty", getDirtyQuests());
        
        NBTTagList tagSchedule = new NBTTagList();
        for(QResetTime entry : getScheduledResets())
        {
            NBTTagCompound tagEntry = new NBTTagCompound();
            tagEntry.setInteger("quest", entry.questID);
            tagEntry.setLong("time", entry.time);
            tagSchedule.appendTag(tagEntry);
        }
        tags.setTag("resetSchedule", tagSchedule);
        
        return tags;
    }
    
    @Override
    public synchronized void deserializeNBT(NBTTagCompound nbt)
    {
        visibleQuests.clear();
        activeQuests.clear();
        resetSchedule.clear();
        autoClaims.clear();
        markedDirty.clear();
        
        for(int i : nbt.getIntArray("visibleQuests")) visibleQuests.add(i);
        for(int i : nbt.getIntArray("activeQuests")) activeQuests.add(i);
        for(int i : nbt.getIntArray("autoClaims")) autoClaims.add(i);
        for(int i : nbt.getIntArray("markedDirty")) markedDirty.add(i);
        
        NBTTagList tagList = nbt.getTagList("resetSchedule", 10);
        for(int i = 0; i < tagList.tagCount(); i++)
        {
            NBTTagCompound tagEntry = tagList.getCompoundTagAt(i);
            if(tagEntry.hasKey("quest", 99))
            {
                resetSchedule.add(new QResetTime(tagEntry.getInteger("quest"), tagEntry.getLong("time")));
            }
        }
    }
    
    public class QResetTime implements Comparable<QResetTime>
    {
        public final int questID;
        public final long time;
        
        private QResetTime(int questID, long time)
        {
            this.questID = questID;
            this.time = time;
        }
    
        @Override
        public int compareTo(QResetTime o)
        {
            return Long.compare(o.time, time);
        }
        
        @Override
        public boolean equals(Object o)
        {
            if(!(o instanceof QResetTime)) return false;
            return ((QResetTime)o).questID == questID;
        }
    }
    
    // TODO: Make this based on a fixed state stored on the quest instead of calculated on demand
    // TODO: Also make this thread safe
    public static boolean isQuestShown(IQuest quest, UUID uuid, EntityPlayer player)
    {
        if(quest == null || uuid == null)
        {
            return false;
        }
        
        EnumQuestVisibility vis = quest.getProperty(NativeProps.VISIBILITY);
        
        if(QuestingAPI.getAPI(ApiReference.SETTINGS).canUserEdit(player) || vis == EnumQuestVisibility.ALWAYS || BQ_Settings.viewMode) // Always shown or in edit mode, or in view mode
        {
            return true;
        } else if(vis == EnumQuestVisibility.HIDDEN)
        {
            return false;
        } else if(vis == EnumQuestVisibility.UNLOCKED)
        {
            return quest.isComplete(uuid) || quest.isUnlocked(uuid);
        } else if(vis == EnumQuestVisibility.NORMAL)
        {
            if(quest.isComplete(uuid) || quest.isUnlocked(uuid)) // Complete or pending
            {
                return true;
            }
            
            // Previous quest is underway and this one is visible but still locked (foreshadowing)
            for(DBEntry<IQuest> q : QuestDatabase.INSTANCE.bulkLookup(quest.getRequirements()))
            {
                if(!q.getValue().isUnlocked(uuid))
                {
                    return false;
                }
            }
            
            return true;
        } else if(vis == EnumQuestVisibility.COMPLETED)
        {
            return quest.isComplete(uuid);
        } else if(vis == EnumQuestVisibility.CHAIN)
        {
            if(quest.getRequirements().length <= 0)
            {
                return true;
            }
            
            for(DBEntry<IQuest> q : QuestDatabase.INSTANCE.bulkLookup(quest.getRequirements()))
            {
                if(q == null) return true;
                
                if(isQuestShown(q.getValue(), uuid, player))
                {
                    return true;
                }
            }
            
            return false;
        }
        
        return true;
    }
}
