package betterquesting.quests;

import java.util.ArrayList;
import java.util.UUID;
import net.minecraft.entity.player.EntityPlayer;
import org.apache.logging.log4j.Level;
import betterquesting.core.BetterQuesting;
import betterquesting.party.PartyInstance;
import betterquesting.party.PartyManager;
import betterquesting.quests.types.QuestBase;
import betterquesting.rewards.RewardBase;
import betterquesting.rewards.RewardRegistry;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

public class QuestInstance
{
	ArrayList<QuestBase> questTypes = new ArrayList<QuestBase>();
	int questID;
	
	JsonObject questSettings = new JsonObject();
	ArrayList<RewardBase> rewards = new ArrayList<RewardBase>();
	
	public String name = "New Quest";
	public String description = "No Description";
	
	ArrayList<UUID> claimed = new ArrayList<UUID>();
	ArrayList<UUID> completeUsers = new ArrayList<UUID>();
	ArrayList<Integer> preRequisites = new ArrayList<Integer>();
	
	boolean globalQuest = false;
	
	public QuestInstance(JsonObject jObj)
	{
		this.readFromJSON(jObj);
		QuestDatabase.questDB.put(this.questID, this);
	}
	
	public QuestInstance()
	{
		this.questID = QuestDatabase.getUniqueID();
		QuestDatabase.questDB.put(this.questID, this);
	}
	
	/**
	 * Quest specific living update event. Do not use for item submissions
	 * @param player
	 */
	public void Update(EntityPlayer player)
	{
		if(this.isUnlocked(player.getUniqueID())) // Prevents quest logic from running until this player has unlocked it
		{
			for(QuestBase quest : questTypes)
			{
				quest.Update(player);
			}
		}
	}
	
	/**
	 * Fired when someone clicks the detect button for this quest
	 */
	public void Detect(EntityPlayer player)
	{
		if(this.isUnlocked(player.getUniqueID()))
		{
			for(QuestBase quest : questTypes)
			{
				quest.Detect(player);
			}
		}
	}
	
	public boolean CanClaim(EntityPlayer player)
	{
		if(claimed.contains(player.getUniqueID()))
		{
			return false;
		} else
		{
			for(RewardBase rew : rewards)
			{
				if(!rew.canClaim(player))
				{
					return false;
				}
			}
		}
		
		return true;
					
	}
	
	public void Claim(EntityPlayer player)
	{
		for(RewardBase rew : rewards)
		{
			rew.Claim(player);
		}
		
		claimed.add(player.getUniqueID());
	}
	
	public void SetGlobal(boolean state)
	{
		this.globalQuest = state;
	}
	
	public boolean isUnlocked(UUID uuid)
	{
		for(int id : preRequisites)
		{
			QuestInstance quest = QuestDatabase.getQuest(id);
			
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
			return completeUsers.contains(uuid);
		}
	}
	
	/**
	 * Sets the complete state of the quest
	 * @param player
	 * @param state
	 */
	public void setCompletion(UUID uuid, boolean state, boolean applyToParty)
	{
		if(state)
		{
			if(!completeUsers.contains(uuid)) // Skip duplicates
			{
				completeUsers.add(uuid);
			}
			
			if(applyToParty)
			{
				for(PartyInstance party : PartyManager.GetParties(uuid))
				{
					for(UUID pId : party.GetMembers())
					{
						if(!completeUsers.contains(pId)) // Skip duplicates
						{
							completeUsers.add(pId);
						}
					}
				}
			}
		} else
		{
			completeUsers.remove(uuid);
			
			if(applyToParty)
			{
				for(PartyInstance party : PartyManager.GetParties(uuid))
				{
					completeUsers.removeAll(party.GetMembers());
				}
			}
		}
	}
	
	/**
	 * Clears all quest data and completion states
	 */
	public void ResetQuest()
	{
		this.claimed.clear();
		this.completeUsers.clear();
	}
	
	public void AddPreRequisite(QuestInstance quest)
	{
		if(!this.preRequisites.contains(quest.questID))
		{
			this.preRequisites.add(quest.questID);
		}
	}
	
	public void RemovePreRequisite(QuestInstance quest)
	{
		this.preRequisites.remove(quest.questID);
	}
	
	public JsonObject GetQuestSettings()
	{
		return this.questSettings;
	}
	
	public void SetQuestSettings(JsonObject jObj)
	{
		this.questSettings = jObj;
	}
	
	public ArrayList<QuestBase> getQuests()
	{
		return questTypes;
	}
	
	public void writeToJSON(JsonObject jObj)
	{
		jObj.addProperty("questID", this.questID);
		
		JsonArray tskJson = new JsonArray();
		for(QuestBase quest : questTypes)
		{
			JsonObject qJson = new JsonObject();
			quest.writeToJson(qJson);
			qJson.addProperty("questID", QuestRegistry.GetID(quest.getClass()));
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
		jObj.add("settings", this.questSettings);
		
		JsonArray comJson = new JsonArray();
		for(UUID uuid : completeUsers)
		{
			comJson.add(new JsonPrimitive(uuid.toString()));
		}
		jObj.add("completed", comJson);
		
		JsonArray reqJson = new JsonArray();
		for(int id : preRequisites)
		{
			reqJson.add(new JsonPrimitive(id));
		}
		jObj.add("preRequisites", reqJson);
		
		JsonArray clmJson = new JsonArray();
		for(UUID uuid : claimed)
		{
			clmJson.add(new JsonPrimitive(uuid.toString()));
		}
		jObj.add("claimed", clmJson);
	}
	
	public void readFromJSON(JsonObject jObj)
	{
		this.questID = jObj.get("questID").getAsInt();
		
		this.questTypes.clear();
		for(JsonElement entry : jObj.getAsJsonArray("tasks"))
		{
			try
			{
				JsonObject jsonQuest = entry.getAsJsonObject();
				QuestBase quest = QuestRegistry.InstatiateQuest(jsonQuest.get("questID").getAsString());
				quest.readFromJson(jsonQuest);
				this.questTypes.add(quest);
			} catch(Exception e)
			{
				BetterQuesting.logger.log(Level.ERROR, "Unable to parse quest from JSON", e);
				continue;
			}
		}
		
		this.rewards.clear();
		for(JsonElement entry : jObj.getAsJsonArray("rewards"))
		{
			try
			{
				JsonObject jsonReward = entry.getAsJsonObject();
				RewardBase reward = RewardRegistry.InstatiateReward(jsonReward.get("rewardID").getAsString());
				reward.readFromJson(jsonReward);
				this.rewards.add(reward);
			} catch(Exception e)
			{
				BetterQuesting.logger.log(Level.ERROR, "Unable to parse reward from JSON", e);
				continue;
			}
		}
		
		this.name = jObj.get("name").getAsString();
		this.description = jObj.get("description").getAsString();
		this.questSettings = jObj.getAsJsonObject("settings");
		
		completeUsers.clear();
		for(JsonElement entry : jObj.getAsJsonArray("completed"))
		{
			completeUsers.add(UUID.fromString(entry.getAsString()));
		}
		
		preRequisites.clear();
		for(JsonElement entry : jObj.getAsJsonArray("preRequisites"))
		{
			preRequisites.add(entry.getAsInt());
		}
		
		claimed.clear();
		for(JsonElement entry : jObj.getAsJsonArray("claimed"))
		{
			claimed.add(UUID.fromString(entry.getAsString()));
		}
	}
}
