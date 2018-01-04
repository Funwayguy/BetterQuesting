package betterquesting.legacy.v0;

import java.util.ArrayList;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import betterquesting.api.enums.EnumLogic;
import betterquesting.api.enums.EnumSaveType;
import betterquesting.api.placeholders.rewards.RewardPlaceholder;
import betterquesting.api.placeholders.tasks.TaskPlaceholder;
import betterquesting.api.properties.IPropertyContainer;
import betterquesting.api.properties.NativeProps;
import betterquesting.api.questing.IQuest;
import betterquesting.api.questing.IQuestLine;
import betterquesting.api.questing.IQuestLineEntry;
import betterquesting.api.questing.rewards.IReward;
import betterquesting.api.questing.tasks.ITask;
import betterquesting.api.storage.IRegStorageBase;
import betterquesting.api.utils.JsonHelper;
import betterquesting.api.utils.NBTConverter;
import betterquesting.legacy.ILegacyLoader;
import betterquesting.questing.QuestDatabase;
import betterquesting.questing.QuestInstance;
import betterquesting.questing.QuestLine;
import betterquesting.questing.QuestLineDatabase;
import betterquesting.questing.QuestLineEntry;
import betterquesting.questing.rewards.RewardRegistry;
import betterquesting.questing.tasks.TaskRegistry;
import betterquesting.storage.QuestSettings;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public final class LegacyLoader_v0 implements ILegacyLoader
{
	public static final LegacyLoader_v0 INSTANCE = new LegacyLoader_v0();
	
	private LegacyLoader_v0()
	{
	}
	
	@Override
	public void readFromJson(JsonElement rawJson, EnumSaveType saveType)
	{
		if(rawJson == null || saveType != EnumSaveType.CONFIG || !rawJson.isJsonObject())
		{
			// Not going to bother with converting progression
			return;
		}
		
		JsonObject json = rawJson.getAsJsonObject();
		
		if(json.has("editMode")) // This IS the file you are looking for
		{
			QuestSettings.INSTANCE.setProperty(NativeProps.EDIT_MODE, JsonHelper.GetBoolean(json, "editMode", true));
			QuestSettings.INSTANCE.setProperty(NativeProps.HARDCORE, JsonHelper.GetBoolean(json, "hardcore", false));
			
			QuestSettings.INSTANCE.setProperty(NativeProps.LIVES_DEF, JsonHelper.GetNumber(json, "defLives", 3).intValue());
			QuestSettings.INSTANCE.setProperty(NativeProps.LIVES_MAX, JsonHelper.GetNumber(json, "maxLives", 10).intValue());
			
			readQuestDatabase(JsonHelper.GetArray(json, "questDatabase"));
			readLineDatabase(JsonHelper.GetArray(json, "questLines"));
		}
	}
	
	public void readQuestDatabase(JsonArray jAry)
	{
		QuestDatabase.INSTANCE.reset();
		
		for(JsonElement je : jAry)
		{
			if(je == null || !je.isJsonObject())
			{
				continue;
			}
			
			JsonObject json = je.getAsJsonObject();
			int qID = JsonHelper.GetNumber(json, "questID", -1).intValue();
			IQuest quest = QuestDatabase.INSTANCE.getValue(qID);
			boolean flag = quest == null;
			quest = quest != null? quest : new QuestInstance();
			readQuest(quest, json);
			
			if(quest != null && flag)
			{
				QuestDatabase.INSTANCE.add(quest, qID);
			}
		}
	}
	
	public void readLineDatabase(JsonArray jAry)
	{
		QuestLineDatabase.INSTANCE.reset();
		
		for(JsonElement je : jAry)
		{
			if(je == null || !je.isJsonObject())
			{
				continue;
			}
			
			IQuestLine qLine = new QuestLine();
			readLine(qLine, je.getAsJsonObject());
			
			QuestLineDatabase.INSTANCE.add(qLine, QuestLineDatabase.INSTANCE.nextKey());
		}
	}
	
	public void readQuest(IQuest quest, JsonObject json)
	{
		IPropertyContainer props = quest.getProperties();
		
		props.setProperty(NativeProps.NAME, JsonHelper.GetString(json, "name", "New Quest"));
		props.setProperty(NativeProps.DESC, JsonHelper.GetString(json, "description", "No Description"));
		props.setProperty(NativeProps.MAIN, JsonHelper.GetBoolean(json, "isMain", false));
		props.setProperty(NativeProps.SILENT, JsonHelper.GetBoolean(json, "isSilent", false));
		props.setProperty(NativeProps.LOCKED_PROGRESS, JsonHelper.GetBoolean(json, "lockedProgress", false));
		props.setProperty(NativeProps.SIMULTANEOUS, JsonHelper.GetBoolean(json, "simultaneous", false));
		props.setProperty(NativeProps.GLOBAL, JsonHelper.GetBoolean(json, "globalQuest", false));
		props.setProperty(NativeProps.GLOBAL_SHARE, JsonHelper.GetBoolean(json, "globalShare", false));
		props.setProperty(NativeProps.AUTO_CLAIM, JsonHelper.GetBoolean(json, "autoClaim", false));
		props.setProperty(NativeProps.REPEAT_TIME, JsonHelper.GetNumber(json, "repeatTime", 2000).intValue());
		props.setProperty(NativeProps.LOGIC_QUEST, EnumLogic.valueOf(JsonHelper.GetString(json, "logic", "AND")));
		props.setProperty(NativeProps.LOGIC_TASK, EnumLogic.valueOf(JsonHelper.GetString(json, "taskLogic", "AND")));
		props.setProperty(NativeProps.ICON, JsonHelper.JsonToItemStack(NBTConverter.JSONtoNBT_Object(JsonHelper.GetObject(json, "icon"), new NBTTagCompound())));
		
		for(JsonElement je : JsonHelper.GetArray(json, "preRequisites"))
		{
			if(je == null || !je.isJsonPrimitive() || !je.getAsJsonPrimitive().isNumber())
			{
				continue;
			}
			
			int qID = je.getAsInt();
			IQuest prq = QuestDatabase.INSTANCE.getValue(qID);
			
			if(prq == null)
			{
				prq = new QuestInstance();
				QuestDatabase.INSTANCE.add(prq, qID);
			}
			
			quest.getPrerequisites().add(prq);
		}
		
		IRegStorageBase<Integer,ITask> taskDB = quest.getTasks();
		ArrayList<ITask> uaTasks = new ArrayList<ITask>();
		
		for(JsonElement entry : JsonHelper.GetArray(json, "tasks"))
		{
			if(entry == null || !entry.isJsonObject())
			{
				continue;
			}
			
			JsonObject jsonTask = entry.getAsJsonObject();
			ResourceLocation loc = new ResourceLocation(JsonHelper.GetString(jsonTask, "taskID", ""));
			int index = JsonHelper.GetNumber(jsonTask, "index", -1).intValue();
			ITask task = TaskRegistry.INSTANCE.createTask(loc);
			
			if(task instanceof TaskPlaceholder)
			{
				JsonObject jt2 = JsonHelper.GetObject(jsonTask, "orig_data");
				ResourceLocation loc2 = new ResourceLocation(JsonHelper.GetString(jt2, "taskID", ""));
				ITask t2 = TaskRegistry.INSTANCE.createTask(loc2);
				
				if(t2 != null) // Restored original task
				{
					jsonTask = jt2;
					task = t2;
				}
			}
			
			NBTTagCompound nbtTask = NBTConverter.JSONtoNBT_Object(jsonTask, new NBTTagCompound());
			
			if(task != null)
			{
				task.readFromNBT(nbtTask, EnumSaveType.CONFIG);
				
				if(index >= 0)
				{
					taskDB.add(task, index);
				} else
				{
					uaTasks.add(task);
				}
			} else
			{
				TaskPlaceholder tph = new TaskPlaceholder();
				tph.setTaskData(nbtTask, EnumSaveType.CONFIG);
				
				if(index >= 0)
				{
					taskDB.add(tph, index);
				} else
				{
					uaTasks.add(tph);
				}
			}
		}
		
		for(ITask t : uaTasks)
		{
			taskDB.add(t, taskDB.nextKey());
		}
		
		IRegStorageBase<Integer,IReward> rewardDB = quest.getRewards();
		ArrayList<IReward> unassigned = new ArrayList<IReward>();
		
		for(JsonElement entry : JsonHelper.GetArray(json, "rewards"))
		{
			if(entry == null || !entry.isJsonObject())
			{
				continue;
			}
			
			JsonObject jsonReward = entry.getAsJsonObject();
			ResourceLocation loc = new ResourceLocation(JsonHelper.GetString(jsonReward, "rewardID", ""));
			int index = JsonHelper.GetNumber(jsonReward, "index", -1).intValue();
			IReward reward = RewardRegistry.INSTANCE.createReward(loc);
			
			if(reward instanceof RewardPlaceholder)
			{
				JsonObject jr2 = JsonHelper.GetObject(jsonReward, "orig_data");
				ResourceLocation loc2 = new ResourceLocation(JsonHelper.GetString(jr2, "rewardID", ""));
				IReward r2 = RewardRegistry.INSTANCE.createReward(loc2);
				
				if(r2 != null)
				{
					jsonReward = jr2;
					reward = r2;
				}
			}
			
			NBTTagCompound nbtReward = NBTConverter.JSONtoNBT_Object(jsonReward, new NBTTagCompound());
			
			if(reward != null)
			{
				reward.readFromNBT(nbtReward, EnumSaveType.CONFIG);
				
				if(index >= 0)
				{
					rewardDB.add(reward, index);
				} else
				{
					unassigned.add(reward);
				}
			} else
			{
				RewardPlaceholder rph = new RewardPlaceholder();
				rph.setRewardData(nbtReward, EnumSaveType.CONFIG);
				
				if(index >= 0)
				{
					rewardDB.add(rph, index);
				} else
				{
					unassigned.add(rph);
				}
			}
		}
		
		for(IReward r : unassigned)
		{
			rewardDB.add(r, rewardDB.nextKey());
		}
	}
	
	public void readLine(IQuestLine qLine, JsonObject json)
	{
		IPropertyContainer props = qLine.getProperties();
		props.setProperty(NativeProps.NAME, JsonHelper.GetString(json, "name", "New Quest Line"));
		props.setProperty(NativeProps.DESC, JsonHelper.GetString(json, "description", "No Description"));
		
		for(JsonElement je : JsonHelper.GetArray(json, "quests"))
		{
			if(je == null || !je.isJsonObject())
			{
				continue;
			}
			
			JsonObject json2 = je.getAsJsonObject();
			
			IQuestLineEntry entry = new QuestLineEntry(JsonHelper.GetNumber(json2, "x", 0).intValue(), JsonHelper.GetNumber(json2, "y", 0).intValue(), 24);
			int qID = JsonHelper.GetNumber(json2, "id", -1).intValue();
			
			if(qID >= 0)
			{
				qLine.add(entry, qID);
			}
		}
	}
}
