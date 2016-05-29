package betterquesting.quests;

import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import betterquesting.lives.LifeManager;
import betterquesting.network.PacketAssembly;
import betterquesting.network.PacketTypeRegistry.BQPacketType;
import betterquesting.quests.tasks.TaskBase;
import betterquesting.utils.JsonHelper;
import betterquesting.utils.NBTConverter;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class QuestDatabase
{
	/**
	 * This is used by UIs to query whether it needs refreshed</br>
	 * Set to false on UI init and when refreshed. It will be reset to true when required
	 */
	public static boolean updateUI = true;
	
	/**
	 * Allows quests to be edited by moderators. Once disabled it can only be re-enabled via editing the JSON file.
	 * Use this to lock down the quest lines before publishing them. This can safely be left on if moderators are
	 * required to edit/maintain quests during use, normal users will have the buttons disabled and any forced edits ignored.
	 */
	public static boolean editMode = true;
	/**
	 * Turns on the hardcore life system
	 */
	public static boolean bqHardcore = false;
	public static volatile ConcurrentHashMap<Integer, QuestInstance> questDB = new ConcurrentHashMap<Integer, QuestInstance>();
	public static volatile CopyOnWriteArrayList<QuestLine> questLines = new CopyOnWriteArrayList<QuestLine>();
	
	/**
	 * @return the next free ID within the quest database
	 */
	public static int getUniqueID()
	{
		int id = 0;
		
		while(questDB.containsKey(id))
		{
			id += 1;
		}
		
		return id;
	}
	
	public static void UpdateTasks(EntityPlayer player)
	{
		for(QuestInstance quest : questDB.values())
		{
			quest.Update(player);
		}
	}
	
	public static QuestInstance GetOrRegisterQuest(int id)
	{
		QuestInstance quest = getQuestByID(id);
		
		if(quest == null)
		{
			quest = new QuestInstance(id, true);
		}
		
		return quest;
	}
	
	public static QuestInstance getQuestByID(int id)
	{
		return questDB.get(id);
	}
	
	public static QuestInstance getQuestByOrder(int index)
	{
		if(index < 0 || index >= questDB.size())
		{
			return null;
		}
		
		return questDB.values().toArray(new QuestInstance[0])[index];
	}
	
	/**
	 * Gets the active quests this UUID currently has or all if UUID is null
	 */
	public static ArrayList<QuestInstance> getActiveQuests(UUID uuid)
	{
		ArrayList<QuestInstance> questList = new ArrayList<QuestInstance>();
		
		for(QuestInstance quest : questDB.values())
		{
			if(quest != null && (quest.lockedProgress || quest.isUnlocked(uuid)) && (!quest.isComplete(uuid) || !quest.HasClaimed(uuid)))
			{
				questList.add(quest);
			}
		}
		
		return questList;
	}
	
	/**
	 * Gets the active tasks this UUID currently has or all tasks if UUID is null
	 */
	public static ArrayList<TaskBase> getActiveTasks(UUID uuid)
	{
		ArrayList<TaskBase> taskList = new ArrayList<TaskBase>();
		
		for(QuestInstance quest : getActiveQuests(uuid))
		{
			for(TaskBase task : quest.tasks)
			{
				if(uuid != null && task.isComplete(uuid))
				{
					continue;
				}
				
				taskList.add(task);
			}
		}
		
		return taskList;
	}

	public static void DeleteQuest(int id)
	{
		QuestInstance quest = getQuestByID(id);
		questDB.remove(id);
		
		// Remove the prerequisites first so the quest redesign doesn't break;
		for(QuestInstance qi :questDB.values())
		{
			qi.preRequisites.remove(quest);
		}
		
		// Remove quest from quest lines and rebuild their trees
		for(QuestLine line : questLines)
		{
			line.questList.remove(quest);
		}
	}
	
	/**
	 * Updates all client's quest databases currently connected to the server.</br>
	 * <b>WARNING:</b> Network intensive! Avoid calling frequently (Use singular quest updates if possible)
	 */
	public static void UpdateClients()
	{
		NBTTagCompound tags = new NBTTagCompound();
		JsonObject json1 = new JsonObject();
		QuestDatabase.writeToJson(json1);
		tags.setTag("Database", NBTConverter.JSONtoNBT_Object(json1, new NBTTagCompound()));
		JsonObject json2 = new JsonObject();
		QuestDatabase.writeToJson_Progression(json2);
		tags.setTag("Progress", NBTConverter.JSONtoNBT_Object(json2, new NBTTagCompound()));
		PacketAssembly.SendToAll(BQPacketType.QUEST_DATABASE.GetLocation(), tags);
	}
	
	/**
	 * Updates specified client's quest databases currently connected to the server.</br>
	 * <b>WARNING:</b> Network intensive! Avoid calling frequently (Use singular quest updates if possible)
	 */
	public static void SendDatabase(EntityPlayerMP player)
	{
		NBTTagCompound tags = new NBTTagCompound();
		JsonObject json1 = new JsonObject();
		QuestDatabase.writeToJson(json1);
		tags.setTag("Database", NBTConverter.JSONtoNBT_Object(json1, new NBTTagCompound()));
		JsonObject json2 = new JsonObject();
		QuestDatabase.writeToJson_Progression(json2);
		tags.setTag("Progress", NBTConverter.JSONtoNBT_Object(json2, new NBTTagCompound()));
		PacketAssembly.SendTo(BQPacketType.QUEST_DATABASE.GetLocation(), tags, player);
	}
	
	public static void writeToJson(JsonObject json)
	{
		writeToJson_Quests(json);
		writeToJson_Lines(json);
	}
	
	public static void readFromJson(JsonObject json)
	{
		readFromJson_Quests(json);
		readFromJson_Lines(json);
	}
	
	public static void writeToJson_Lines(JsonObject json)
	{
		JsonArray jArray = new JsonArray();
		for(QuestLine line : questLines)
		{
			JsonObject tmp = new JsonObject();
			line.writeToJSON(tmp);
			jArray.add(tmp);
		}
		json.add("questLines", jArray);
	}
	
	public static void readFromJson_Lines(JsonObject json)
	{
		updateUI = true;
		questLines.clear();
		for(JsonElement entry : JsonHelper.GetArray(json, "questLines"))
		{
			if(entry == null || !entry.isJsonObject())
			{
				continue;
			}
			
			QuestLine line = new QuestLine();
			line.readFromJSON(entry.getAsJsonObject());
			questLines.add(line);
		}
	}
	
	public static void writeToJson_Quests(JsonObject json)
	{
		json.addProperty("editMode", editMode);
		json.addProperty("hardcore", bqHardcore);
		json.addProperty("defLives", LifeManager.defLives);
		json.addProperty("maxLives", LifeManager.maxLives);
		
		JsonArray dbJson = new JsonArray();
		for(QuestInstance quest : questDB.values())
		{
			JsonObject questJson = new JsonObject();
			quest.writeToJSON(questJson);
			dbJson.add(questJson);
		}
		json.add("questDatabase", dbJson);
	}
	
	public static void readFromJson_Quests(JsonObject json)
	{
		if(json == null)
		{
			json = new JsonObject();
		}
		
		editMode = JsonHelper.GetBoolean(json, "editMode", true);
		bqHardcore = JsonHelper.GetBoolean(json, "hardcore", false);
		LifeManager.defLives = JsonHelper.GetNumber(json, "defLives", 3).intValue();
		LifeManager.maxLives = JsonHelper.GetNumber(json, "maxLives", 10).intValue();
		
		updateUI = true;
		questDB.clear();
		
		for(JsonElement entry : JsonHelper.GetArray(json, "questDatabase"))
		{
			if(entry == null || !entry.isJsonObject())
			{
				continue;
			}
			
			int qID = JsonHelper.GetNumber(entry.getAsJsonObject(), "questID", -1).intValue();
			
			if(qID < 0)
			{
				continue;
			}
			
			QuestInstance quest = GetOrRegisterQuest(qID);
			quest.readFromJSON(entry.getAsJsonObject());
		}
	}
	
	public static void writeToJson_Progression(JsonObject json)
	{
		JsonArray dbJson = new JsonArray();
		for(QuestInstance quest : questDB.values())
		{
			JsonObject questJson = new JsonObject();
			quest.writeProgressToJSON(questJson);
			dbJson.add(questJson);
		}
		json.add("questProgress", dbJson);
	}
	
	public static void readFromJson_Progression(JsonObject json)
	{
		for(JsonElement entry : JsonHelper.GetArray(json, "questProgress"))
		{
			if(entry == null || !entry.isJsonObject())
			{
				continue;
			}
			
			int qID = JsonHelper.GetNumber(entry.getAsJsonObject(), "questID", -1).intValue();
			
			if(qID < 0)
			{
				continue;
			}
			
			QuestInstance quest = GetOrRegisterQuest(qID);
			quest.readProgressFromJSON(entry.getAsJsonObject());
		}
	}
}
