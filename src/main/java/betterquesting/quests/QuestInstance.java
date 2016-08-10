package betterquesting.quests;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import net.minecraftforge.common.MinecraftForge;
import org.apache.logging.log4j.Level;
import betterquesting.api.enums.EnumLogic;
import betterquesting.api.enums.EnumQuestVisibility;
import betterquesting.api.enums.EnumSaveType;
import betterquesting.api.network.PacketTypeNative;
import betterquesting.api.quests.IQuestContainer;
import betterquesting.api.quests.rewards.IRewardBase;
import betterquesting.api.quests.tasks.ITaskBase;
import betterquesting.api.utils.BigItemStack;
import betterquesting.api.utils.JsonHelper;
import betterquesting.api.utils.NBTConverter;
import betterquesting.core.BetterQuesting;
import betterquesting.network.PacketAssembly;
import betterquesting.network.PacketSender;
import betterquesting.party.PartyInstance;
import betterquesting.party.PartyInstance.PartyMember;
import betterquesting.party.PartyManager;
import betterquesting.registry.RewardRegistry;
import betterquesting.registry.TaskRegistry;
import betterquesting.utils.UserEntry;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class QuestInstance implements IQuestContainer
{
	private BigItemStack itemIcon = new BigItemStack(Items.nether_star);
	private EnumQuestVisibility visibility = EnumQuestVisibility.NORMAL;
	private final TaskStorage tasks = new TaskStorage();
	private final RewardStorage rewards = new RewardStorage();
	
	private String name = "New Quest";
	private String description = "No Description";
	
	private ArrayList<UserEntry> completeUsers = new ArrayList<UserEntry>();
	private ArrayList<Integer> preRequisites = new ArrayList<Integer>();
	
	private EnumLogic logic = EnumLogic.AND;
	private EnumLogic tLogic = EnumLogic.AND;
	private boolean globalQuest = false;
	private boolean globalShare = true;
	private float globalParticipation = 0F;
	private boolean autoClaim = false;
	private int repeatTime = -1;
	
	private boolean isMain = false;
	private boolean isSilent = false;
	private boolean lockedProgress = false;
	private boolean simultaneous = false;
	
	private String sndComplete = "random.levelup";
	private String sndUpdate = "random.levelup";
	private String sndUnlock = "random.click"; // Not currently used
	
	/**
	 * Quest specific living update event. Do not use for item submissions
	 */
	@Override
	public void update(EntityPlayer player)
	{
		if(isComplete(player.getUniqueID()))
		{
			UserEntry entry = GetUserEntry(player.getUniqueID());
			
			if(!HasClaimed(player.getUniqueID()))
			{
				if(canClaim(player))
				{
					// Quest is complete and pending claim.
					// Task logic is not required to run.
					if(autoClaim && player.ticksExisted%20 == 0)
					{
						claimReward(player);
					}
					
					return;
				} else if(repeatTime < 0 || rewards.size() <= 0)
				{
					// Task is non repeatable or has no rewards to claim
					return;
				} else
				{
					// Task logic will now run for repeat quest
				}
			} else if(rewards.size() > 0 && repeatTime >= 0 && player.worldObj.getTotalWorldTime() - entry.getTimestamp() >= repeatTime)
			{
				// Task is scheduled to reset
				if(globalQuest)
				{
					ResetAllProgress();
				} else
				{
					ResetProgress(player.getUniqueID());
				}
				
				if(!QuestProperties.INSTANCE.isEditMode() && !isSilent)
				{
					NBTTagCompound tags = new NBTTagCompound();
					tags.setString("Main", "betterquesting.notice.update");
					tags.setString("Sub", name);
					tags.setString("Sound", sndUpdate);
					tags.setTag("Icon", itemIcon.writeToNBT(new NBTTagCompound()));
					
					if(globalQuest)
					{
						PacketSender.INSTANCE.sendToAll(PacketTypeNative.NOTIFICATION.GetLocation(), tags);
					} else if(player instanceof EntityPlayerMP)
					{
						PacketSender.INSTANCE.sendToPlayer(PacketTypeNative.NOTIFICATION.GetLocation(), tags, (EntityPlayerMP)player);
					}
				}
				
				syncAll();
				return;
			} else
			{
				// No reset or reset is pending
				return;
			}
		}
		
		if(isUnlocked(player.getUniqueID()) || lockedProgress)
		{
			int done = 0;
			boolean update = false;
			
			for(ITaskBase tsk : tasks)
			{
				boolean flag = !tsk.isComplete(player.getUniqueID());
				
				tsk.update(player, this);
				
				if(tsk.isComplete(player.getUniqueID()))
				{
					done += 1;
					
					if(flag)
					{
						update = true;
					}
				}
			}
			
			if(!isUnlocked(player.getUniqueID()))
			{
				if(update)
				{
					syncAll();
				}
				
				return;
			} else if((tasks.size() > 0 || !QuestProperties.INSTANCE.isEditMode()) && tLogic.GetResult(done, tasks.size()))
			{
				setComplete(player.getUniqueID(), player.worldObj.getTotalWorldTime());
				
				syncAll();
				
				if(!QuestProperties.INSTANCE.isEditMode() && !isSilent)
				{
					NBTTagCompound tags = new NBTTagCompound();
					tags.setString("Main", "betterquesting.notice.complete");
					tags.setString("Sub", name);
					tags.setString("Sound", sndComplete);
					tags.setTag("Icon", itemIcon.writeToNBT(new NBTTagCompound()));
					
					if(globalQuest)
					{
						PacketSender.INSTANCE.sendToAll(PacketTypeNative.NOTIFICATION.GetLocation(), tags);
					} else if(player instanceof EntityPlayerMP)
					{
						PacketSender.INSTANCE.sendToPlayer(PacketTypeNative.NOTIFICATION.GetLocation(), tags, (EntityPlayerMP)player);
					}
				}
			} else if(update && simultaneous)
			{
				ResetProgress(player.getUniqueID());
				syncAll();
			} else if(update)
			{
				syncAll();
				
				if(!QuestDatabase.editMode && !isSilent)
				{
					NBTTagCompound tags = new NBTTagCompound();
					tags.setString("Main", "betterquesting.notice.update");
					tags.setString("Sub", name);
					tags.setString("Sound", sndUpdate);
					tags.setTag("Icon", itemIcon.writeToNBT(new NBTTagCompound()));
					
					if(globalQuest)
					{
						PacketSender.INSTANCE.sendToAll(PacketTypeNative.NOTIFICATION.GetLocation(), tags);
					} else if(player instanceof EntityPlayerMP)
					{
						PacketSender.INSTANCE.sendToPlayer(PacketTypeNative.NOTIFICATION.GetLocation(), tags, (EntityPlayerMP)player);
					}
				}
			}
		}
	}
	
	/**
	 * Fired when someone clicks the detect button for this quest
	 */
	@Override
	public void detect(EntityPlayer player)
	{
		if(isComplete(player.getUniqueID()) && (repeatTime < 0 || rewards.size() <= 0))
		{
			return;
		} else if(!canSubmit(player))
		{
			return;
		}
		
		if(isUnlocked(player.getUniqueID()) || QuestProperties.INSTANCE.isEditMode())
		{
			int done = 0;
			boolean update = false;
			
			for(ITaskBase tsk : tasks)
			{
				boolean flag = !tsk.isComplete(player.getUniqueID());
				
				tsk.detect(player, this);
				
				if(tsk.isComplete(player.getUniqueID()))
				{
					done += 1;
					
					if(flag)
					{
						update = true;
					}
				}
			}
			
			if((tasks.size() > 0 || !QuestDatabase.editMode) && tLogic.GetResult(done, tasks.size()))
			{
				setComplete(player.getUniqueID(), player.worldObj.getTotalWorldTime());
				
				if(!QuestDatabase.editMode && !isSilent)
				{
					NBTTagCompound tags = new NBTTagCompound();
					tags.setString("Main", "betterquesting.notice.complete");
					tags.setString("Sub", name);
					tags.setString("Sound", sndComplete);
					tags.setTag("Icon", itemIcon.writeToNBT(new NBTTagCompound()));
					
					if(globalQuest)
					{
						PacketSender.INSTANCE.sendToAll(PacketTypeNative.NOTIFICATION.GetLocation(), tags);
					} else if(player instanceof EntityPlayerMP)
					{
						PacketSender.INSTANCE.sendToPlayer(PacketTypeNative.NOTIFICATION.GetLocation(), tags, (EntityPlayerMP)player);
					}
				}
			} else if(update && simultaneous)
			{
				ResetProgress(player.getUniqueID());
				syncAll();
			} else if(update)
			{
				if(!QuestDatabase.editMode && !isSilent)
				{
					NBTTagCompound tags = new NBTTagCompound();
					tags.setString("Main", "betterquesting.notice.update");
					tags.setString("Sub", name);
					tags.setString("Sound", sndUpdate);
					tags.setTag("Icon", itemIcon.writeToNBT(new NBTTagCompound()));
					
					if(globalQuest)
					{
						PacketSender.INSTANCE.sendToAll(PacketTypeNative.NOTIFICATION.GetLocation(), tags);
					} else if(player instanceof EntityPlayerMP)
					{
						PacketSender.INSTANCE.sendToPlayer(PacketTypeNative.NOTIFICATION.GetLocation(), tags, (EntityPlayerMP)player);
					}
				}
			}
			
			syncAll();
		}
	}
	
	public boolean HasClaimed(UUID uuid)
	{
		if(rewards.size() <= 0)
		{
			return true;
		}
				
		if(globalQuest)
		{
			if(GetParticipation(uuid) < globalParticipation)
			{
				return true;
			} else if(!globalShare)
			{
				for(UserEntry entry : completeUsers)
				{
					if(entry.hasClaimed())
					{
						return true;
					}
				}
				
				return false;
			}
		}
		
		UserEntry entry = GetUserEntry(uuid);
		
		if(entry == null)
		{
			return false;
		}
		
		return entry.hasClaimed();
	}
	
	@Override
	public boolean canClaim(EntityPlayer player)
	{
		UserEntry entry = GetUserEntry(player.getUniqueID());
		
		if(entry == null || HasClaimed(player.getUniqueID()))
		{
			return false;
		} else if(canSubmit(player))
		{
			return false;
		} else
		{
			for(int i = 0; i < rewards.size(); i++)
			{
				IRewardBase rew = rewards.get(i);
				
				if(!rew.canClaim(player, this))
				{
					return false;
				}
			}
		}
		
		return true;
	}
	
	@Override
	public void claimReward(EntityPlayer player)
	{
		for(int i = 0; i < rewards.size(); i++)
		{
			IRewardBase rew = rewards.get(i);
			
			rew.claimReward(player, this);
		}
		
		UserEntry entry = GetUserEntry(player.getUniqueID());
		entry.setClaimed(true, player.worldObj.getTotalWorldTime());
		
		syncAll();
	}
	
	@Override
	public boolean canSubmit(EntityPlayer player)
	{
		if(player == null)
		{
			return false;
		}
		
		UserEntry entry = this.GetUserEntry(player.getUniqueID());
		
		if(entry == null)
		{
			return true;
		} else if(!entry.hasClaimed())
		{
			int done = 0;
			
			for(ITaskBase tsk : tasks)
			{
				if(tsk.isComplete(player.getUniqueID()))
				{
					done += 1;
				}
			}
			
			return !tLogic.GetResult(done, tasks.size());
		} else
		{
			return false;
		}
	}
	
	public float GetParticipation(UUID uuid)
	{
		if(tasks.size() <= 0)
		{
			return 0F;
		}
		
		float total = 0F;
		
		for(ITaskBase t : tasks)
		{
			total += t.GetParticipation(uuid);
		}
		
		return total / tasks.size();
	}
	
	@SideOnly(Side.CLIENT)
	public ArrayList<String> getStandardTooltip(EntityPlayer player)
	{
		ArrayList<String> list = new ArrayList<String>();
		
		list.add(StatCollector.translateToLocalFormatted(name));
		
		if(isComplete(player.getUniqueID()))
		{
			list.add(EnumChatFormatting.GREEN + StatCollector.translateToLocalFormatted("betterquesting.tooltip.complete"));
			
			if(!HasClaimed(player.getUniqueID()))
			{
				list.add(EnumChatFormatting.GRAY + StatCollector.translateToLocalFormatted("betterquesting.tooltip.rewards_pending"));
			} else if(repeatTime > 0)
			{
				long time = getRepeatSeconds(player);
				DecimalFormat df = new DecimalFormat("00");
				list.add(EnumChatFormatting.GRAY + StatCollector.translateToLocalFormatted("betterquesting.tooltip.repeat", (time/60) + "m " + df.format(time%60) + "s"));
			}
		} else if(!isUnlocked(player.getUniqueID()))
		{
			list.add(EnumChatFormatting.RED + "" + EnumChatFormatting.UNDERLINE + StatCollector.translateToLocalFormatted("betterquesting.tooltip.requires") + " (" + logic.toString().toUpperCase() + ")");
			
			for(QuestInstance req : preRequisites)
			{
				if(!req.isComplete(player.getUniqueID()))
				{
					list.add(EnumChatFormatting.RED + "- " + StatCollector.translateToLocalFormatted(req.name));
				}
			}
		} else
		{
			int n = 0;
			
			for(TaskBase task : tasks)
			{
				if(task.isComplete(player.getUniqueID()))
				{
					n++;
				}
			}
			
			list.add(EnumChatFormatting.GRAY + StatCollector.translateToLocalFormatted("betterquesting.tooltip.tasks_complete", n, tasks.size()));
		}
		
		list.add(EnumChatFormatting.DARK_GRAY + StatCollector.translateToLocalFormatted("betterquesting.tooltip.shift_advanced"));
		
		return list;
	}
	
	@SideOnly(Side.CLIENT)
	public ArrayList<String> getAdvancedTooltip(EntityPlayer player)
	{
		ArrayList<String> list = new ArrayList<String>();
		
		list.add(StatCollector.translateToLocalFormatted(name) + " #" + questID);
		
		list.add(EnumChatFormatting.GRAY + StatCollector.translateToLocalFormatted("betterquesting.tooltip.main_quest", isMain));
		list.add(EnumChatFormatting.GRAY + StatCollector.translateToLocalFormatted("betterquesting.tooltip.global_quest", globalQuest));
		if(globalQuest)
		{
			list.add(EnumChatFormatting.GRAY + StatCollector.translateToLocalFormatted("betterquesting.tooltip.global_share", globalShare));
		}
		list.add(EnumChatFormatting.GRAY + StatCollector.translateToLocalFormatted("betterquesting.tooltip.task_logic", logic.toString().toUpperCase()));
		list.add(EnumChatFormatting.GRAY + StatCollector.translateToLocalFormatted("betterquesting.tooltip.simultaneous", simultaneous));
		list.add(EnumChatFormatting.GRAY + StatCollector.translateToLocalFormatted("betterquesting.tooltip.auto_claim", autoClaim));
		if(repeatTime >= 0)
		{
			DecimalFormat df = new DecimalFormat("00");
			list.add(EnumChatFormatting.GRAY + StatCollector.translateToLocalFormatted("betterquesting.tooltip.repeat", (repeatTime/60) + "m " + df.format(repeatTime%60) + "s"));
		} else
		{
			list.add(EnumChatFormatting.GRAY + StatCollector.translateToLocalFormatted("betterquesting.tooltip.repeat", false));
		}
		
		return list;
	}
	
	@SideOnly(Side.CLIENT)
	public long getRepeatSeconds(EntityPlayer player)
	{
		if(repeatTime < 0)
		{
			return -1;
		}
		
		UserEntry ue = GetUserEntry(player.getUniqueID());
		
		if(ue == null)
		{
			return 0;
		} else
		{
			return (repeatTime - (player.worldObj.getTotalWorldTime() - ue.getTimestamp()))/20L;
		}
	}
	
	@Override
	public void syncAll()
	{
		NBTTagCompound tags = new NBTTagCompound();
		tags.setInteger("questID", this.questID);
		JsonObject json1 = new JsonObject();
		writeToJson(json1, EnumSaveType.CONFIG);
		tags.setTag("Data", NBTConverter.JSONtoNBT_Object(json1, new NBTTagCompound()));
		JsonObject json2 = new JsonObject();
		writeProgressToJSON(json2);
		tags.setTag("Progress", NBTConverter.JSONtoNBT_Object(json2, new NBTTagCompound()));
		PacketSender.INSTANCE.sendToAll(PacketTypeNative.QUEST_SYNC.GetLocation(), tags);
	}
	
	public boolean isUnlocked(UUID uuid)
	{
		int A = 0;
		int B = preRequisites.size();
		
		if(B <= 0)
		{
			return true;
		}
		
		for(QuestInstance quest : preRequisites)
		{
			if(quest != null && quest.isComplete(uuid))
			{
				A++;
			}
		}
		
		return logic.GetResult(A, B);
	}
	
	public void setComplete(UUID uuid, long timestamp)
	{
		PartyInstance party = PartyManager.GetParty(uuid);
		
		if(party == null)
		{
			UserEntry entry = this.GetUserEntry(uuid);
			
			if(entry != null)
			{
				entry.setClaimed(false, timestamp);
			} else
			{
				completeUsers.add(new UserEntry(uuid, timestamp));
			}
		} else
		{
			for(PartyMember mem : party.GetMembers())
			{
				UserEntry entry = this.GetUserEntry(mem.userID);
				
				if(entry != null)
				{
					entry.setClaimed(false, timestamp);
				} else
				{
					completeUsers.add(new UserEntry(mem.userID, timestamp));
				}
			}
		}
	}
	
	/**
	 * Returns true if the quest has been completed at least once
	 */
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
		boolean flag = false;
		
		for(int i = completeUsers.size() - 1; i >= 0; i--)
		{
			UserEntry entry = completeUsers.get(i);
			
			for(UUID id : uuid)
			{
				if(entry.getUUID().equals(id))
				{
					completeUsers.remove(i);
					flag = true;
					break;
				}
			}
		}
		
		if(flag)
		{
			syncAll();
		}
	}
	
	public UserEntry GetUserEntry(UUID uuid)
	{
		for(UserEntry entry : completeUsers)
		{
			if(entry.getUUID().equals(uuid))
			{
				return entry;
			}
		}
		
		return null;
	}
	
	/**
	 * Clears all quest data and completion states
	 */
	public void ResetQuest()
	{
		this.completeUsers = new ArrayList<UserEntry>();
		
		for(TaskBase t : tasks)
		{
			t.ResetAllProgress();
		}
	}
	
	/**
	 * Clears all quest data and completion status for only this user
	 */
	public void ResetQuest(UUID uuid)
	{
		for(TaskBase t : tasks)
		{
			t.ResetProgress(uuid);
			t.setCompletion(uuid, false);
		}
		
		RemoveUserEntry(uuid);
	}
	
	/**
	 * Resets task progress and claim status but does not reset completion status (applies to party members too)
	 */
	public void ResetProgress(UUID uuid)
	{
		PartyInstance party = PartyManager.GetParty(uuid);
		
		if(party == null)
		{
			UserEntry entry = GetUserEntry(uuid);
			
			if(entry != null)
			{
				entry.setClaimed(false, 0);
			}
			
			for(TaskBase t : tasks)
			{
				t.ResetProgress(uuid);
			}
		} else
		{
			for(PartyMember mem : party.GetMembers())
			{
				UserEntry entry = GetUserEntry(mem.userID);
				
				if(entry != null)
				{
					entry.setClaimed(false, 0);
				}
				
				for(TaskBase t : tasks)
				{
					t.ResetProgress(mem.userID);
				}
			}
		}
	}
	
	/**
	 * Resets task progress and claim status for all users
	 */
	public void ResetAllProgress()
	{
		for(UserEntry entry : completeUsers)
		{
			entry.setClaimed(false, 0);
		}
		
		for(TaskBase t : tasks)
		{
			t.ResetAllProgress();
		}
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
	
	@Override
	public JsonObject writeToJson(JsonObject json, EnumSaveType saveType)
	{
		switch(saveType)
		{
			case CONFIG:
				writeToJson_Config(json);
				break;
			case PROGRESS:
				writeToJson_Progress(json);
				break;
			default:
				break;
		}
		
		return json;
	}
	
	@Override
	public void readFromJson(JsonObject json, EnumSaveType saveType)
	{
		switch(saveType)
		{
			case CONFIG:
				readFromJson_Config(json);
				break;
			case PROGRESS:
				readFromJson_Progress(json);
				break;
			default:
				break;
		}
	}
	
	private JsonObject writeToJson_Config(JsonObject jObj)
	{
		jObj.addProperty("name", name);
		jObj.addProperty("description", description);
		jObj.addProperty("isMain", isMain);
		jObj.addProperty("isSilent", isSilent);
		jObj.addProperty("lockedProgress", lockedProgress);
		jObj.addProperty("simultaneous", simultaneous);
		jObj.addProperty("globalParticipation", globalParticipation);
		jObj.addProperty("globalQuest", globalQuest);
		jObj.addProperty("globalShare", globalShare);
		jObj.addProperty("autoClaim", autoClaim);
		jObj.addProperty("repeatTime", repeatTime);
		
		JsonObject jSounds = new JsonObject();
		jSounds.addProperty("complete", sndComplete);
		jSounds.addProperty("update", sndUpdate);
		//jSounds.addProperty("unlock", sndUnlock); // Not implemented
		jObj.add("sounds", jSounds);
		
		jObj.addProperty("logic", logic.toString());
		jObj.addProperty("taskLogic", tLogic.toString());
		jObj.add("icon", JsonHelper.ItemStackToJson(itemIcon, new JsonObject()));
		jObj.addProperty("visibility", visibility.toString());
		
		JsonArray tskJson = tasks.writeToJson(new JsonArray(), EnumSaveType.CONFIG);
		jObj.add("tasks", tskJson);
		
		JsonArray rwdJson = rewards.writeToJson(new JsonArray(), EnumSaveType.CONFIG);
		jObj.add("rewards", rwdJson);
		
		JsonArray reqJson = new JsonArray();
		for(int quest : preRequisites)
		{
			reqJson.add(new JsonPrimitive(quest));
		}
		jObj.add("preRequisites", reqJson);
		
		return jObj;
	}
	
	private void readFromJson_Config(JsonObject jObj)
	{
		this.name = JsonHelper.GetString(jObj, "name", "New Quest");
		this.description = JsonHelper.GetString(jObj, "description", "No Description");
		this.isMain = JsonHelper.GetBoolean(jObj, "isMain", false);
		this.isSilent = JsonHelper.GetBoolean(jObj, "isSilent", false);
		this.lockedProgress = JsonHelper.GetBoolean(jObj, "lockedProgress", false);
		this.simultaneous = JsonHelper.GetBoolean(jObj, "simultaneous", false);
		this.globalParticipation = JsonHelper.GetNumber(jObj, "globalParticipation", 0F).floatValue();
		this.globalQuest = JsonHelper.GetBoolean(jObj, "globalQuest", false);
		this.globalShare = JsonHelper.GetBoolean(jObj, "globalShare", true);
		this.autoClaim = JsonHelper.GetBoolean(jObj, "autoClaim", false);
		this.repeatTime = JsonHelper.GetNumber(jObj, "repeatTime", -1).intValue();
		
		JsonObject jSounds = JsonHelper.GetObject(jObj, "sounds");
		this.sndComplete = JsonHelper.GetString(jSounds, "complete", "random.levelup");
		this.sndUpdate = JsonHelper.GetString(jSounds, "update", "random.levelup");
		//this.sndUnlock = JsonHelper.GetString(jSounds, "unlock", "random.click"); // Not implemented
		
		try
		{
			this.logic = EnumLogic.valueOf(JsonHelper.GetString(jObj, "logic", "AND").toUpperCase());
			this.logic = logic == null? EnumLogic.AND : logic;
		} catch(Exception e)
		{
			this.logic = EnumLogic.AND;
		}
		
		try
		{
			this.tLogic = EnumLogic.valueOf(JsonHelper.GetString(jObj, "taskLogic", "AND").toUpperCase());
			this.tLogic = tLogic == null? EnumLogic.AND : tLogic;
		} catch(Exception e)
		{
			this.tLogic = EnumLogic.AND;
		}
		
		this.itemIcon = JsonHelper.JsonToItemStack(JsonHelper.GetObject(jObj, "icon"));
		this.itemIcon = this.itemIcon != null? this.itemIcon : new BigItemStack(Items.nether_star);
		
		try
		{
			this.visibility = EnumQuestVisibility.valueOf(JsonHelper.GetString(jObj, "visibility", "AND").toUpperCase());
			this.visibility = visibility == null? EnumQuestVisibility.NORMAL : visibility;
		} catch(Exception e)
		{
			this.visibility = EnumQuestVisibility.NORMAL;
		}
		
		this.tasks.readFromJson(JsonHelper.GetArray(jObj, "tasks"), EnumSaveType.CONFIG);
		
		this.rewards.readFromJson(JsonHelper.GetArray(jObj, "rewards"), EnumSaveType.CONFIG);
		
		preRequisites = new ArrayList<Integer>();
		for(JsonElement entry : JsonHelper.GetArray(jObj, "preRequisites"))
		{
			if(entry == null || !entry.isJsonPrimitive() || !entry.getAsJsonPrimitive().isNumber())
			{
				continue;
			}
			
			preRequisites.add(entry.getAsInt());
		}
		
		// Backwards compatibility with single quest files
		if(jObj.has("completed"))
		{
			jMig = jObj;
		}
	}
	
	private JsonObject writeToJson_Progress(JsonObject json)
	{
		JsonArray comJson = new JsonArray();
		for(UserEntry entry : completeUsers)
		{
			comJson.add(entry.writeToJson(new JsonObject()));
		}
		json.add("completed", comJson);
		
		JsonArray tskJson = tasks.writeToJson(new JsonArray(), EnumSaveType.PROGRESS);
		json.add("tasks", tskJson);
		
		return json;
	}
	
	JsonObject jMig = null;
	
	private void readFromJson_Progress(JsonObject json)
	{
		JsonObject jTmp = jMig != null? jMig : json; // Check for migrated progress
		jMig = null;
		
		completeUsers = new ArrayList<UserEntry>();
		for(JsonElement entry : JsonHelper.GetArray(jTmp, "completed"))
		{
			if(entry == null || !entry.isJsonObject())
			{
				continue;
			}
			
			try
			{
				UUID uuid = UUID.fromString(JsonHelper.GetString(entry.getAsJsonObject(), "uuid", ""));
				UserEntry user = new UserEntry(uuid);
				user.readFromJson(entry.getAsJsonObject());
				completeUsers.add(user);
			} catch(Exception e)
			{
				BetterQuesting.logger.log(Level.ERROR, "Unable to load UUID for quest", e);
			}
		}
		
		tasks.readFromJson(JsonHelper.GetArray(json, "tasks"), EnumSaveType.PROGRESS);
	}
}
