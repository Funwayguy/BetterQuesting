package betterquesting.questing;

import betterquesting.api.api.QuestingAPI;
import betterquesting.api.enums.EnumLogic;
import betterquesting.api.enums.EnumQuestState;
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
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.nbt.*;
import net.minecraftforge.common.util.Constants;
import org.apache.logging.log4j.Level;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;

public class QuestInstance implements IQuest {
    private final TaskStorage tasks = new TaskStorage();
    private final RewardStorage rewards = new RewardStorage();

    private final HashMap<UUID, NBTTagCompound> completeUsers = new HashMap<>();
    private int[] preRequisites = new int[0];
    private final TIntObjectMap<RequirementType> prereqTypes = new TIntObjectHashMap<>();

    private final PropertyContainer qInfo = new PropertyContainer();

    public QuestInstance() {
        this.setupProps();
    }

    private void setupProps() {
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
        setupValue(NativeProps.VISIBILITY, NativeProps.VISIBILITY.getDefault());
    }

    private <T> void setupValue(IPropertyType<T> prop) {
        this.setupValue(prop, prop.getDefault());
    }

    private <T> void setupValue(IPropertyType<T> prop, T def) {
        qInfo.setProperty(prop, qInfo.getProperty(prop, def));
    }

    @Override
    public void update(EntityPlayer player) {
        UUID playerID = QuestingAPI.getQuestingUUID(player);

        int done = 0;

        for (DBEntry<ITask> entry : tasks.getEntries()) {
            if (entry.getValue().isComplete(playerID) || entry.getValue().ignored(playerID)) {
                done++;
            }
        }

        if (tasks.size() <= 0 || qInfo.getProperty(NativeProps.LOGIC_TASK).getResult(done, tasks.size())) {
            setComplete(playerID, System.currentTimeMillis());
        } else if (done > 0 && qInfo.getProperty(NativeProps.SIMULTANEOUS)) // TODO: There is actually an exploit here to do with locked progression bypassing simultaneous reset conditions. Fix?
        {
            resetUser(playerID, false);
        }
    }

    /**
     * Fired when someone clicks the detect button for this quest
     */
    @Override
    public void detect(EntityPlayer player) {
        UUID playerID = QuestingAPI.getQuestingUUID(player);
        QuestCache qc = player.getCapability(CapabilityProviderQuestCache.CAP_QUEST_CACHE, null);
        if (qc == null) return;
        int questID = QuestDatabase.INSTANCE.getID(this);

        if (isComplete(playerID) && (qInfo.getProperty(NativeProps.REPEAT_TIME) < 0 || rewards.size() <= 0)) {
            return;
        } else if (!canSubmit(player)) {
            return;
        }

        if (isUnlocked(playerID) || QuestSettings.INSTANCE.getProperty(NativeProps.EDIT_MODE)) {
            int done = 0;
            boolean update = false;

            ParticipantInfo partInfo = new ParticipantInfo(player);
            DBEntry<IQuest> dbe = new DBEntry<>(questID, this);

            for (DBEntry<ITask> entry : tasks.getEntries()) {
                if (!entry.getValue().isComplete(playerID) || !entry.getValue().ignored(playerID)) {
                    entry.getValue().detect(partInfo, dbe);

                    if (entry.getValue().isComplete(playerID) || entry.getValue().ignored(playerID)) {
                        done++;
                        update = true;
                    }
                } else {
                    done++;
                }
            }
            // Note: Tasks can mark the quest dirty themselves if progress changed but hasn't fully completed.
            if (tasks.size() <= 0 || qInfo.getProperty(NativeProps.LOGIC_TASK).getResult(done, tasks.size())) {
                // State won't be auto updated in edit mode so we force change it here and mark it for re-sync
                if (QuestSettings.INSTANCE.getProperty(NativeProps.EDIT_MODE))
                    setComplete(playerID, System.currentTimeMillis());
                qc.markQuestDirty(questID);
            } else if (update && qInfo.getProperty(NativeProps.SIMULTANEOUS)) {
                resetUser(playerID, false);
                qc.markQuestDirty(questID);
            } else if (update) {
                qc.markQuestDirty(questID);
            }
        }
    }

    @Override
    public boolean hasClaimed(UUID uuid) {
        if (rewards.size() <= 0) return true;

        synchronized (completeUsers) {
            if (qInfo.getProperty(NativeProps.GLOBAL) && !qInfo.getProperty(NativeProps.GLOBAL_SHARE)) {
                // TODO: Figure out some replacement to track participation
                for (NBTTagCompound entry : completeUsers.values()) {
                    if (entry.getBoolean("claimed")) return true;
                }

                return false;
            }

            NBTTagCompound entry = getCompletionInfo(uuid);
            return entry != null && entry.getBoolean("claimed");
        }
    }

    @Override
    public boolean canClaimBasically(EntityPlayer player) {
        UUID pID = QuestingAPI.getQuestingUUID(player);
        NBTTagCompound entry = getCompletionInfo(pID);

        return entry != null && !hasClaimed(pID) && !canSubmit(player);
    }

    @Override
    public boolean canClaim(EntityPlayer player) {
        if (!canClaimBasically(player)) return false;
        DBEntry<IQuest> dbe = new DBEntry<>(QuestDatabase.INSTANCE.getID(this), this);
        for (DBEntry<IReward> rew : rewards.getEntries()) {
            if (!rew.getValue().canClaim(player, dbe)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public void claimReward(EntityPlayer player) {
        int questID = QuestDatabase.INSTANCE.getID(this);
        DBEntry<IQuest> dbe = new DBEntry<>(questID, this);
        for (DBEntry<IReward> rew : rewards.getEntries()) {
            rew.getValue().claimReward(player, dbe);
        }

        UUID pID = QuestingAPI.getQuestingUUID(player);
        QuestCache qc = player.getCapability(CapabilityProviderQuestCache.CAP_QUEST_CACHE, null);

        synchronized (completeUsers) {
            NBTTagCompound entry = getCompletionInfo(pID);

            if (entry == null) {
                entry = new NBTTagCompound();
                this.completeUsers.put(pID, entry);
            }

            entry.setBoolean("claimed", true);
            entry.setLong("timestamp", System.currentTimeMillis());
        }

        if (qc != null) qc.markQuestDirty(questID);
    }

    @Override
    public boolean canSubmit(@Nonnull EntityPlayer player) {
        UUID playerID = QuestingAPI.getQuestingUUID(player);

        synchronized (completeUsers) {
            NBTTagCompound entry = this.getCompletionInfo(playerID);
            if (entry == null) return true;

            if (!entry.getBoolean("claimed") && getProperty(NativeProps.REPEAT_TIME) >= 0) // Complete but repeatable
            {
                if (tasks.size() <= 0) return true;

                int done = 0;

                for (DBEntry<ITask> tsk : tasks.getEntries()) {
                    if (tsk.getValue().isComplete(playerID) || tsk.getValue().ignored(playerID)) {
                        done += 1;
                    }
                }

                return !qInfo.getProperty(NativeProps.LOGIC_TASK).getResult(done, tasks.size());
            } else {
                return false;
            }
        }
    }

    @Override
    public boolean isUnlocked(UUID uuid) {
        if (preRequisites.length <= 0) return true;

        int A = 0;
        int B = preRequisites.length;

        for (DBEntry<IQuest> quest : QuestDatabase.INSTANCE.bulkLookup(getRequirements())) {
            if (quest.getValue().isComplete(uuid)) {
                A++;
            }
        }

        return qInfo.getProperty(NativeProps.LOGIC_QUEST).getResult(A, B);
    }

    @Override
    public void setComplete(UUID uuid, long timestamp) {
        if (uuid == null) return;

        synchronized (completeUsers) {
            NBTTagCompound entry = this.getCompletionInfo(uuid);

            if (entry == null) {
                entry = new NBTTagCompound();
                completeUsers.put(uuid, entry);
            }

            entry.setBoolean("claimed", false);
            entry.setLong("timestamp", timestamp);
        }
    }

    /**
     * Returns true if the quest has been completed at least once
     */
    @Override
    public boolean isComplete(UUID uuid) {
        if (qInfo.getProperty(NativeProps.GLOBAL)) {
            return completeUsers.size() > 0;
        } else {
            return getCompletionInfo(uuid) != null;
        }
    }

    @Override
    public EnumQuestState getState(EntityPlayer player) {
        UUID uuid = QuestingAPI.getQuestingUUID(player);
        if (this.isComplete(uuid)) {
            if (canClaimBasically(player)) {
                return EnumQuestState.UNCLAIMED;
            } else if (this.getProperty(NativeProps.REPEAT_TIME) > -1 && !this.hasClaimed(uuid)) {
                return EnumQuestState.REPEATABLE;
            }
            return EnumQuestState.COMPLETED;
        } else if (this.isUnlocked(uuid)) {
            return EnumQuestState.UNLOCKED;
        }

        return EnumQuestState.LOCKED;
    }

    @Override
    public NBTTagCompound getCompletionInfo(UUID uuid) {
        synchronized (completeUsers) {
            return completeUsers.get(uuid);
        }
    }

    @Override
    public void setCompletionInfo(UUID uuid, NBTTagCompound nbt) {
        if (uuid == null) return;

        synchronized (completeUsers) {
            if (nbt == null) {
                completeUsers.remove(uuid);
            } else {
                completeUsers.put(uuid, nbt);
            }
        }
    }

    /**
     * Resets task progress and claim status. If performing a full reset, completion status will also be erased
     */
    @Override
    public void resetUser(@Nullable UUID uuid, boolean fullReset) {
        synchronized (completeUsers) {
            if (fullReset) {
                if (uuid == null) {
                    completeUsers.clear();
                } else {
                    completeUsers.remove(uuid);
                }
            } else {
                if (uuid == null) {
                    completeUsers.forEach((key, value) -> {
                        value.setBoolean("claimed", false);
                        value.setLong("timestamp", 0);
                    });
                } else {
                    NBTTagCompound entry = getCompletionInfo(uuid);
                    if (entry != null) {
                        entry.setBoolean("claimed", false);
                        entry.setLong("timestamp", 0);
                    }
                }
            }

            tasks.getEntries().forEach((value) -> value.getValue().resetUser(uuid));
        }
    }

    @Override
    public IDatabaseNBT<ITask, NBTTagList, NBTTagList> getTasks() {
        return tasks;
    }

    @Override
    public IDatabaseNBT<IReward, NBTTagList, NBTTagList> getRewards() {
        return rewards;
    }

    @Nonnull
    @Override
    public int[] getRequirements() {
        return this.preRequisites;
    }

    public void setRequirements(@Nonnull int[] req) {
        Arrays.sort(req);
        prereqTypes.retainEntries((a, b) -> Arrays.binarySearch(req, a) >= 0);
        this.preRequisites = req;
    }

    @Nonnull
    @Override
    public RequirementType getRequirementType(int req) {
        RequirementType type = prereqTypes.get(req);
        return type == null ? RequirementType.NORMAL : type;
    }

    @Override
    public void setRequirementType(int req, @Nonnull RequirementType kind) {
        if (kind == RequirementType.NORMAL)
            prereqTypes.remove(req);
        else
            prereqTypes.put(req, kind);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound jObj) {
        jObj.setTag("properties", qInfo.writeToNBT(new NBTTagCompound()));
        jObj.setTag("tasks", tasks.writeToNBT(new NBTTagList(), null));
        jObj.setTag("rewards", rewards.writeToNBT(new NBTTagList(), null));
        jObj.setTag("preRequisites", new NBTTagIntArray(getRequirements()));
        if (!prereqTypes.isEmpty()) {
            byte[] types = new byte[preRequisites.length];
            int[] req = this.preRequisites;
            for (int i = 0, requirementsLength = req.length; i < requirementsLength; i++)
                types[i] = getRequirementType(req[i]).id();
            jObj.setTag("preRequisiteTypes", new NBTTagByteArray(types));
        }

        return jObj;
    }

    @Override
    public void readFromNBT(NBTTagCompound jObj) {
        this.qInfo.readFromNBT(jObj.getCompoundTag("properties"));
        this.tasks.readFromNBT(jObj.getTagList("tasks", 10), false);
        this.rewards.readFromNBT(jObj.getTagList("rewards", 10), false);

        if (jObj.getTagId("preRequisites") == Constants.NBT.TAG_INT_ARRAY) // Native NBT
        {
            setRequirements(jObj.getIntArray("preRequisites"));
        } else // Probably an NBTTagList
        {
            NBTTagList rList = jObj.getTagList("preRequisites", 4);
            int[] req = new int[rList.tagCount()];
            for (int i = 0; i < rList.tagCount(); i++) {
                NBTBase pTag = rList.get(i);
                req[i] = pTag instanceof NBTPrimitive ? ((NBTPrimitive) pTag).getInt() : -1;
            }
            setRequirements(req);
        }

        if (jObj.getTagId("preRequisiteTypes") == Constants.NBT.TAG_BYTE_ARRAY) {
            int[] reqs = getRequirements();
            byte[] byteArray = jObj.getByteArray("preRequisiteTypes");
            for (int i = 0, byteArrayLength = byteArray.length; i < byteArrayLength && i < reqs.length; i++)
                setRequirementType(reqs[i], RequirementType.from(byteArray[i]));
        } else {
            prereqTypes.clear();
        }

        this.setupProps();
    }

    @Override
    public NBTTagCompound writeProgressToNBT(NBTTagCompound json, @Nullable List<UUID> users) {
        synchronized (completeUsers) {
            NBTTagList comJson = new NBTTagList();
            for (Entry<UUID, NBTTagCompound> entry : completeUsers.entrySet()) {
                if (users != null && !users.contains(entry.getKey())) continue;
                NBTTagCompound tags = entry.getValue().copy();
                tags.setString("uuid", entry.getKey().toString());
                comJson.appendTag(tags);
            }
            json.setTag("completed", comJson);

            NBTTagList tskJson = tasks.writeProgressToNBT(new NBTTagList(), users);
            json.setTag("tasks", tskJson);

            return json;
        }
    }

    @Override
    public void readProgressFromNBT(NBTTagCompound json, boolean merge) {
        synchronized (completeUsers) {
            if (!merge) completeUsers.clear();
            NBTTagList comList = json.getTagList("completed", 10);
            for (int i = 0; i < comList.tagCount(); i++) {
                NBTTagCompound entry = comList.getCompoundTagAt(i).copy();

                try {
                    UUID uuid = UUID.fromString(entry.getString("uuid"));
                    completeUsers.put(uuid, entry);
                } catch (Exception e) {
                    BetterQuesting.logger.log(Level.ERROR, "Unable to load UUID for quest", e);
                }
            }

            tasks.readProgressFromNBT(json.getTagList("tasks", 10), merge);
        }
    }

    @Override
    public void setClaimed(UUID uuid, long timestamp) {
        synchronized (completeUsers) {
            NBTTagCompound entry = this.getCompletionInfo(uuid);

            if (entry != null) {
                entry.setBoolean("claimed", true);
                entry.setLong("timestamp", timestamp);
            } else {
                entry = new NBTTagCompound();
                entry.setBoolean("claimed", true);
                entry.setLong("timestamp", timestamp);
                completeUsers.put(uuid, entry);
            }
        }
    }

    @Override
    public <T> T getProperty(IPropertyType<T> prop) {
        return qInfo.getProperty(prop);
    }

    @Override
    public <T> T getProperty(IPropertyType<T> prop, T def) {
        return qInfo.getProperty(prop, def);
    }

    @Override
    public boolean hasProperty(IPropertyType<?> prop) {
        return qInfo.hasProperty(prop);
    }

    @Override
    public <T> void setProperty(IPropertyType<T> prop, T value) {
        qInfo.setProperty(prop, value);
    }

    @Override
    public void removeProperty(IPropertyType<?> prop) {
        qInfo.removeProperty(prop);
    }

    @Override
    public void removeAllProps() {
        qInfo.removeAllProps();
    }
}
