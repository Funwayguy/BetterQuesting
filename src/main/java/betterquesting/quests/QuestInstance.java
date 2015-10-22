package betterquesting.quests;

import java.util.ArrayList;
import java.util.UUID;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import org.apache.logging.log4j.Level;
import betterquesting.core.BetterQuesting;
import betterquesting.network.PacketQuesting;
import betterquesting.party.PartyInstance;
import betterquesting.party.PartyManager;
import betterquesting.quests.rewards.RewardBase;
import betterquesting.quests.rewards.RewardRegistry;
import betterquesting.quests.tasks.TaskBase;
import betterquesting.quests.tasks.TaskRegistry;
import betterquesting.utils.JsonHelper;
import betterquesting.utils.NBTConverter;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class QuestInstance
{
	public int questID;
	public ArrayList<TaskBase> questTypes = new ArrayList<TaskBase>();
	public ArrayList<RewardBase> rewards = new ArrayList<RewardBase>();
	
	public String name = "New Quest";
	public String description = "No Description";
	
	ArrayList<UserEntry> completeUsers = new ArrayList<UserEntry>();
	public ArrayList<QuestInstance> preRequisites = new ArrayList<QuestInstance>();
	
	boolean globalQuest = false;
	
	public QuestInstance(int questID, boolean register)
	{
		this.questID = questID;
		
		if(register)
		{
			QuestDatabase.questDB.put(this.questID, this);
		}
	}
	
	/**
	 * Quest specific living update event. Do not use for item submissions
	 * @param player
	 */
	public void Update(EntityPlayer player)
	{
		if(this.isComplete(player.getUniqueID()))
		{
			return;
		}
		
		if(this.isUnlocked(player.getUniqueID())) // Prevents quest logic from running until this player has unlocked it
		{
			boolean done = true;
			
			for(TaskBase quest : questTypes)
			{
				quest.Update(player);
				
				if(!quest.isComplete(player))
				{
					done = false;
				}
			}
			
			if(done)
			{
				UserEntry entry = new UserEntry(player.getUniqueID());
				entry.timestamp = player.worldObj.getTotalWorldTime();
				completeUsers.add(entry);
				UpdateClients();
			}
		}
	}
	
	/**
	 * Fired when someone clicks the detect button for this quest
	 */
	public void Detect(EntityPlayer player)
	{
		if(this.isComplete(player.getUniqueID()))
		{
			return;
		}
		
		if(this.isUnlocked(player.getUniqueID()))
		{
			boolean done = true;
			
			for(TaskBase quest : questTypes)
			{
				quest.Detect(player);
				
				if(!quest.isComplete(player))
				{
					done = false;
				}
			}
			
			if(done)
			{
				completeUsers.add(new UserEntry(player));
			}
			
			UpdateClients(); // Even if not completed we still need to update progression for clients
		}
	}
	
	public boolean CanClaim(EntityPlayer player, NBTTagList choiceData)
	{
		UserEntry entry = GetUserEntry(player.getUniqueID());
		
		if(entry == null || entry.claimed)
		{
			return false;
		} else
		{
			for(int i = 0; i < rewards.size(); i++)
			{
				RewardBase rew = rewards.get(i);
				NBTTagCompound cTag = choiceData.getCompoundTagAt(i);
				
				if(!rew.canClaim(player, cTag))
				{
					return false;
				}
			}
		}
		
		return true;
					
	}
	
	public void Claim(EntityPlayer player, NBTTagList choiceData)
	{
		for(int i = 0; i < rewards.size(); i++)
		{
			RewardBase rew = rewards.get(i);
			NBTTagCompound cTag = choiceData.getCompoundTagAt(i);
			
			rew.Claim(player, cTag);
		}
		
		UserEntry entry = GetUserEntry(player.getUniqueID());
		entry.claimed = true;
		entry.timestamp = player.worldObj.getTotalWorldTime();
		
		UpdateClients();
	}
	
	@SideOnly(Side.CLIENT)
	public NBTTagList GetChoiceData()
	{
		NBTTagList cList = new NBTTagList();
		
		for(RewardBase rew : rewards)
		{
			cList.appendTag(rew.GetChoiceData());
		}
		
		return cList;
	}
	
	public void UpdateClients()
	{
		NBTTagCompound tags = new NBTTagCompound();
		tags.setInteger("ID", 1);
		tags.setInteger("questID", this.questID);
		JsonObject json = new JsonObject();
		writeToJSON(json);
		tags.setTag("Data", NBTConverter.JSONtoNBT_Object(json, new NBTTagCompound()));
		BetterQuesting.instance.network.sendToAll(new PacketQuesting(tags));
	}
	
	public void SetGlobal(boolean state)
	{
		this.globalQuest = state;
	}
	
	public boolean isUnlocked(UUID uuid)
	{
		for(QuestInstance quest : preRequisites)
		{
			if(quest != null && !quest.isComplete(uuid))
			{
				return false;
			}
		}
		
		return true;
	}
	
	public boolean isComplete(UUID uuid)
	{
		if(this.globalQuest)
		{
			return completeUsers.size() > 0;
		} else
		{
			return GetUserEntry(uuid) != null;
		}
	}
	
	public void RemoveUserEntry(UUID... uuid)
	{
		for(int i = completeUsers.size() - 1; i >= 0; i--)
		{
			UserEntry entry = completeUsers.get(i);
			
			if(entry.uuid.equals(uuid))
			{
				completeUsers.remove(i);
				UpdateClients();
			}
		}
	}
	
	public UserEntry GetUserEntry(UUID uuid)
	{
		for(UserEntry entry : completeUsers)
		{
			if(entry.uuid.equals(uuid))
			{
				return entry;
			}
		}
		
		return null;
	}
	
	/**
	 * Sets the complete state of the quest
	 */
	public void setCompletion(UUID uuid, long timestamp, boolean state, boolean applyToParty)
	{
		boolean flag = false;
		
		if(state)
		{
			UserEntry entry = GetUserEntry(uuid);
			
			if(entry == null) // Skip duplicates
			{
				completeUsers.add(new UserEntry(uuid));
				flag = true;
			}
			
			if(applyToParty)
			{
				PartyInstance party = PartyManager.GetParty(uuid);
				
				if(party != null)
				{
					for(UUID pId : party.GetMembers())
					{
						UserEntry pEntry = GetUserEntry(pId);
						
						if(pEntry == null) // Skip duplicates
						{
							completeUsers.add(new UserEntry(pId));
							flag = true;
						}
					}
				}
			}
		} else
		{
			RemoveUserEntry(uuid);
			
			if(applyToParty)
			{
				PartyInstance party = PartyManager.GetParty(uuid);
				
				if(party != null)
				{
					RemoveUserEntry(party.GetMembers().toArray(new UUID[]{}));
				}
			}
			
			flag = true;
		}
		
		if(flag)
		{
			UpdateClients();
		}
	}
	
	/**
	 * Clears all quest data and completion states
	 */
	public void ResetQuest()
	{
		this.completeUsers.clear();
	}
	
	public void AddPreRequisite(QuestInstance quest)
	{
		if(!this.preRequisites.contains(quest.questID))
		{
			this.preRequisites.add(quest);
		}
	}
	
	public void RemovePreRequisite(QuestInstance quest)
	{
		this.preRequisites.remove(quest);
	}
	
	public ArrayList<TaskBase> getQuests()
	{
		return questTypes;
	}
	
	public void writeToJSON(JsonObject jObj)
	{
		jObj.addProperty("questID", this.questID);
		
		JsonArray tskJson = new JsonArray();
		for(TaskBase quest : questTypes)
		{
			String taskID = TaskRegistry.GetID(quest.getClass());
			
			if(taskID == null)
			{
				BetterQuesting.logger.log(Level.ERROR, "A quest was unable to save an unregistered task: " + quest.getClass().getName());
				continue;
			}
			
			JsonObject qJson = new JsonObject();
			quest.writeToJson(qJson);
			qJson.addProperty("taskID", TaskRegistry.GetID(quest.getClass()));
			tskJson.add(qJson);
		}
		jObj.add("tasks", tskJson);
		
		JsonArray rwdJson = new JsonArray();
		for(RewardBase rew : rewards)
		{
			JsonObject rJson = new JsonObject();
			rew.writeToJson(rJson);
			rJson.addProperty("rewardID", RewardRegistry.GetID(rew.getClass()));
			rwdJson.add(rJson);
		}
		jObj.add("rewards", rwdJson);
		
		jObj.addProperty("name", this.name);
		jObj.addProperty("description", this.description);
		
		JsonArray comJson = new JsonArray();
		for(UserEntry entry : completeUsers)
		{
			comJson.add(entry.toJson());
		}
		jObj.add("completed", comJson);
		
		JsonArray reqJson = new JsonArray();
		for(QuestInstance quest : preRequisites)
		{
			if(quest == null)
			{
				BetterQuesting.logger.log(Level.ERROR, "Quest had null prequisite!", new IllegalArgumentException());
				continue;
			}
			reqJson.add(new JsonPrimitive(quest.questID));
		}
		jObj.add("preRequisites", reqJson);
	}
	
	public void readFromJSON(JsonObject jObj)
	{
		this.questID = JsonHelper.GetNumber(jObj, "questID", -1).intValue();
		
		this.questTypes.clear();
		for(JsonElement entry : JsonHelper.GetArray(jObj, "tasks"))
		{
			if(entry == null || !entry.isJsonObject())
			{
				continue;
			}
			
			JsonObject jsonQuest = entry.getAsJsonObject();
			TaskBase quest = TaskRegistry.InstatiateQuest(JsonHelper.GetString(jsonQuest, "taskID", ""));
			
			if(quest != null)
			{
				quest.readFromJson(jsonQuest);
				this.questTypes.add(quest);
			}
		}
		
		this.rewards.clear();
		for(JsonElement entry : JsonHelper.GetArray(jObj, "rewards"))
		{
			if(entry == null || !entry.isJsonObject())
			{
				continue;
			}
			
			JsonObject jsonReward = entry.getAsJsonObject();
			RewardBase reward = RewardRegistry.InstatiateReward(JsonHelper.GetString(jsonReward, "rewardID", ""));
			
			if(reward != null)
			{
				reward.readFromJson(jsonReward);
				this.rewards.add(reward);
			}
		}
		
		this.name = JsonHelper.GetString(jObj, "name", "New Quest");
		this.description = JsonHelper.GetString(jObj, "description", "No Description");
		
		completeUsers.clear();
		for(JsonElement entry : JsonHelper.GetArray(jObj, "completed"))
		{
			if(entry == null || !entry.isJsonObject())
			{
				continue;
			}
			
			try
			{
				UUID uuid = UUID.fromString(JsonHelper.GetString(entry.getAsJsonObject(), "uuid", ""));
				UserEntry user = new UserEntry(uuid);
				user.fromJson(entry.getAsJsonObject());
				completeUsers.add(user);
			} catch(Exception e)
			{
				BetterQuesting.logger.log(Level.ERROR, "Unable to load UUID '" + entry.getAsString() + "' for quest", e);
			}
		}
		
		preRequisites.clear();
		for(JsonElement entry : JsonHelper.GetArray(jObj, "preRequisites"))
		{
			preRequisites.add(QuestDatabase.getQuest(entry.getAsInt()));
		}
	}
	
	private class UserEntry
	{
		public final UUID uuid;
		public long timestamp = 0;
		public boolean claimed = false;
		
		public UserEntry(EntityPlayer player)
		{
			this.uuid = player.getUniqueID();
			this.timestamp = player.worldObj.getTotalWorldTime();
		}
		
		public UserEntry(UUID uuid)
		{
			this.uuid = uuid;
		}
		
		public JsonObject toJson()
		{
			JsonObject json = new JsonObject();
			json.addProperty("uuid", uuid.toString());
			json.addProperty("timestamp", timestamp);
			json.addProperty("claimed", claimed);
			return json;
		}
		
		public void fromJson(JsonObject json)
		{
			timestamp = JsonHelper.GetNumber(json, "timestamp", 0).longValue();
			claimed = JsonHelper.GetBoolean(json, "claimed", false);
		}
	}
}
