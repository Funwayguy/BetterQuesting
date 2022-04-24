package betterquesting.legacy.v0;

import betterquesting.api.enums.EnumLogic;
import betterquesting.api.placeholders.rewards.RewardPlaceholder;
import betterquesting.api.placeholders.tasks.TaskPlaceholder;
import betterquesting.api.properties.NativeProps;
import betterquesting.api.questing.IQuest;
import betterquesting.api.questing.IQuestLine;
import betterquesting.api.questing.IQuestLineEntry;
import betterquesting.api.questing.rewards.IReward;
import betterquesting.api.questing.tasks.ITask;
import betterquesting.api.utils.JsonHelper;
import betterquesting.api.utils.NBTConverter;
import betterquesting.api2.storage.IDatabaseNBT;
import betterquesting.legacy.ILegacyLoader;
import betterquesting.questing.*;
import betterquesting.questing.rewards.RewardRegistry;
import betterquesting.questing.tasks.TaskRegistry;
import betterquesting.storage.QuestSettings;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public final class LegacyLoader_v0 implements ILegacyLoader {
    public static final LegacyLoader_v0 INSTANCE = new LegacyLoader_v0();

    private LegacyLoader_v0() {
    }

    @Override
    public void readFromJson(JsonElement rawJson) {
        if (rawJson == null || !rawJson.isJsonObject()) {
            // Not going to bother with converting progression
            return;
        }

        JsonObject json = rawJson.getAsJsonObject();

        if (json.has("editMode")) // This IS the file you are looking for
        {
            QuestSettings.INSTANCE.setProperty(NativeProps.EDIT_MODE, JsonHelper.GetBoolean(json, "editMode", true));
            QuestSettings.INSTANCE.setProperty(NativeProps.HARDCORE, JsonHelper.GetBoolean(json, "hardcore", false));

            QuestSettings.INSTANCE.setProperty(NativeProps.LIVES_DEF, JsonHelper.GetNumber(json, "defLives", 3).intValue());
            QuestSettings.INSTANCE.setProperty(NativeProps.LIVES_MAX, JsonHelper.GetNumber(json, "maxLives", 10).intValue());

            readQuestDatabase(JsonHelper.GetArray(json, "questDatabase"));
            readLineDatabase(JsonHelper.GetArray(json, "questLines"));
        }
    }

    public void readQuestDatabase(JsonArray jAry) {
        QuestDatabase.INSTANCE.reset();

        for (JsonElement je : jAry) {
            if (je == null || !je.isJsonObject()) {
                continue;
            }

            JsonObject json = je.getAsJsonObject();
            int qID = JsonHelper.GetNumber(json, "questID", -1).intValue();
            IQuest quest = QuestDatabase.INSTANCE.getValue(qID);
            boolean flag = quest == null;
            quest = quest != null ? quest : new QuestInstance();
            readQuest(quest, json);

            if (flag) {
                QuestDatabase.INSTANCE.add(qID, quest);
            }
        }
    }

    @Override
    public void readProgressFromJson(JsonElement json) {
        if (!json.isJsonObject()) return;
        QuestDatabase.INSTANCE.readProgressFromNBT(NBTConverter.JSONtoNBT_Object(json.getAsJsonObject(), new NBTTagCompound(), true).getTagList("questProgress", 10), false);
    }

    public void readLineDatabase(JsonArray jAry) {
        QuestLineDatabase.INSTANCE.reset();

        for (JsonElement je : jAry) {
            if (je == null || !je.isJsonObject()) {
                continue;
            }

            IQuestLine qLine = new QuestLine();
            readLine(qLine, je.getAsJsonObject());

            QuestLineDatabase.INSTANCE.add(QuestLineDatabase.INSTANCE.nextID(), qLine);
        }
    }

    public void readQuest(IQuest quest, JsonObject json) {
        quest.setProperty(NativeProps.NAME, JsonHelper.GetString(json, "name", "New Quest"));
        quest.setProperty(NativeProps.DESC, JsonHelper.GetString(json, "description", "No Description"));
        quest.setProperty(NativeProps.MAIN, JsonHelper.GetBoolean(json, "isMain", false));
        quest.setProperty(NativeProps.SILENT, JsonHelper.GetBoolean(json, "isSilent", false));
        quest.setProperty(NativeProps.LOCKED_PROGRESS, JsonHelper.GetBoolean(json, "lockedProgress", false));
        quest.setProperty(NativeProps.SIMULTANEOUS, JsonHelper.GetBoolean(json, "simultaneous", false));
        quest.setProperty(NativeProps.GLOBAL, JsonHelper.GetBoolean(json, "globalQuest", false));
        quest.setProperty(NativeProps.GLOBAL_SHARE, JsonHelper.GetBoolean(json, "globalShare", false));
        quest.setProperty(NativeProps.AUTO_CLAIM, JsonHelper.GetBoolean(json, "autoClaim", false));
        quest.setProperty(NativeProps.REPEAT_TIME, JsonHelper.GetNumber(json, "repeatTime", 2000).intValue());
        quest.setProperty(NativeProps.LOGIC_QUEST, EnumLogic.valueOf(JsonHelper.GetString(json, "logic", "AND")));
        quest.setProperty(NativeProps.LOGIC_TASK, EnumLogic.valueOf(JsonHelper.GetString(json, "taskLogic", "AND")));
        quest.setProperty(NativeProps.ICON, JsonHelper.JsonToItemStack(NBTConverter.JSONtoNBT_Object(JsonHelper.GetObject(json, "icon"), new NBTTagCompound(), true)));

        JsonArray reqAry = JsonHelper.GetArray(json, "preRequisites");
        int[] req = new int[reqAry.size()];
        for (int i = 0; i < req.length; i++) {
            JsonElement je = reqAry.get(i);
            if (je == null || !je.isJsonPrimitive() || !je.getAsJsonPrimitive().isNumber()) {
                req[i] = -1;
            } else {
                req[i] = je.getAsInt();
            }
        }
        quest.setRequirements(req);

        IDatabaseNBT<ITask, NBTTagList, NBTTagList> taskDB = quest.getTasks();
        List<ITask> uaTasks = new ArrayList<>();

        for (JsonElement entry : JsonHelper.GetArray(json, "tasks")) {
            if (entry == null || !entry.isJsonObject()) {
                continue;
            }

            JsonObject jsonTask = entry.getAsJsonObject();
            ResourceLocation loc = new ResourceLocation(JsonHelper.GetString(jsonTask, "taskID", ""));
            int index = JsonHelper.GetNumber(jsonTask, "index", -1).intValue();
            ITask task = TaskRegistry.INSTANCE.createNew(loc);

            if (task instanceof TaskPlaceholder) {
                JsonObject jt2 = JsonHelper.GetObject(jsonTask, "orig_data");
                ResourceLocation loc2 = new ResourceLocation(JsonHelper.GetString(jt2, "taskID", ""));
                ITask t2 = TaskRegistry.INSTANCE.createNew(loc2);

                if (t2 != null) // Restored original task
                {
                    jsonTask = jt2;
                    task = t2;
                }
            }

            NBTTagCompound nbtTask = NBTConverter.JSONtoNBT_Object(jsonTask, new NBTTagCompound(), true);

            if (task != null) {
                task.readFromNBT(nbtTask);

                if (index >= 0) {
                    taskDB.add(index, task);
                } else {
                    uaTasks.add(task);
                }
            } else {
                TaskPlaceholder tph = new TaskPlaceholder();
                tph.setTaskConfigData(nbtTask);

                if (index >= 0) {
                    taskDB.add(index, tph);
                } else {
                    uaTasks.add(tph);
                }
            }
        }

        for (ITask t : uaTasks) {
            taskDB.add(taskDB.nextID(), t);
        }

        IDatabaseNBT<IReward, NBTTagList, NBTTagList> rewardDB = quest.getRewards();
        List<IReward> unassigned = new ArrayList<>();

        for (JsonElement entry : JsonHelper.GetArray(json, "rewards")) {
            if (entry == null || !entry.isJsonObject()) {
                continue;
            }

            JsonObject jsonReward = entry.getAsJsonObject();
            ResourceLocation loc = new ResourceLocation(JsonHelper.GetString(jsonReward, "rewardID", ""));
            int index = JsonHelper.GetNumber(jsonReward, "index", -1).intValue();
            IReward reward = RewardRegistry.INSTANCE.createNew(loc);

            if (reward instanceof RewardPlaceholder) {
                JsonObject jr2 = JsonHelper.GetObject(jsonReward, "orig_data");
                ResourceLocation loc2 = new ResourceLocation(JsonHelper.GetString(jr2, "rewardID", ""));
                IReward r2 = RewardRegistry.INSTANCE.createNew(loc2);

                if (r2 != null) {
                    jsonReward = jr2;
                    reward = r2;
                }
            }

            NBTTagCompound nbtReward = NBTConverter.JSONtoNBT_Object(jsonReward, new NBTTagCompound(), true);

            if (reward != null) {
                reward.readFromNBT(nbtReward);

                if (index >= 0) {
                    rewardDB.add(index, reward);
                } else {
                    unassigned.add(reward);
                }
            } else {
                RewardPlaceholder rph = new RewardPlaceholder();
                rph.setRewardConfigData(nbtReward);

                if (index >= 0) {
                    rewardDB.add(index, rph);
                } else {
                    unassigned.add(rph);
                }
            }
        }

        for (IReward r : unassigned) {
            rewardDB.add(rewardDB.nextID(), r);
        }
    }

    public void readLine(IQuestLine qLine, JsonObject json) {
        qLine.setProperty(NativeProps.NAME, JsonHelper.GetString(json, "name", "New Quest Line"));
        qLine.setProperty(NativeProps.DESC, JsonHelper.GetString(json, "description", "No Description"));

        for (JsonElement je : JsonHelper.GetArray(json, "quests")) {
            if (je == null || !je.isJsonObject()) {
                continue;
            }

            JsonObject json2 = je.getAsJsonObject();

            IQuestLineEntry entry = new QuestLineEntry(JsonHelper.GetNumber(json2, "x", 0).intValue(), JsonHelper.GetNumber(json2, "y", 0).intValue(), 24, 24);
            int qID = JsonHelper.GetNumber(json2, "id", -1).intValue();

            if (qID >= 0) {
                qLine.add(qID, entry);
            }
        }
    }
}
