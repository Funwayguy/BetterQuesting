package betterquesting.quests;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;
import org.apache.logging.log4j.Level;
import org.lwjgl.input.Keyboard;
import betterquesting.api.database.IRegStorage;
import betterquesting.api.enums.EnumQuestState;
import betterquesting.api.enums.EnumSaveType;
import betterquesting.api.network.PacketTypeNative;
import betterquesting.api.network.PreparedPayload;
import betterquesting.api.party.IParty;
import betterquesting.api.quests.IQuestContainer;
import betterquesting.api.quests.IQuestSound;
import betterquesting.api.quests.properties.IQuestInfo;
import betterquesting.api.quests.properties.QuestProperties;
import betterquesting.api.quests.rewards.IRewardBase;
import betterquesting.api.quests.tasks.IProgression;
import betterquesting.api.quests.tasks.ITaskBase;
import betterquesting.api.utils.BigItemStack;
import betterquesting.api.utils.JsonHelper;
import betterquesting.api.utils.NBTConverter;
import betterquesting.core.BetterQuesting;
import betterquesting.network.PacketSender;
import betterquesting.party.PartyManager;
import betterquesting.utils.UserEntry;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class QuestInstance implements IQuestContainer
{
	private final TaskStorage tasks = new TaskStorage();
	private final RewardStorage rewards = new RewardStorage();
	
	private final ArrayList<UserEntry> completeUsers = new ArrayList<UserEntry>();
	private final ArrayList<IQuestContainer> preRequisites = new ArrayList<IQuestContainer>();
	
	private QuestInfo qInfo = new QuestInfo();
	private IQuestSound qSounds = new QuestSound();
	
	@Override
	public String getUnlocalisedName()
	{
		String def = "quest.untitled.name";
		
		if(!qInfo.hasProperty(QuestProperties.NAME))
		{
			qInfo.setProperty(QuestProperties.NAME, def);
			return def;
		}
		
		return qInfo.getProperty(QuestProperties.NAME, def);
	}
	
	@Override
	public String getUnlocalisedDescription()
	{
		String def = "quest.untitled.desc";
		
		if(!qInfo.hasProperty(QuestProperties.DESC))
		{
			qInfo.setProperty(QuestProperties.DESC, def);
			return def;
		}
		
		return qInfo.getProperty(QuestProperties.DESC, def);
	}
	
	@Override
	public BigItemStack getItemIcon()
	{
		BigItemStack def = new BigItemStack(Items.nether_star);
		
		if(!qInfo.hasProperty(QuestProperties.ICON))
		{
			qInfo.setProperty(QuestProperties.ICON, def);
			return def;
		}
		
		return qInfo.getProperty(QuestProperties.ICON, def);
	}
	
	@Override
	public IQuestInfo getInfo()
	{
		return qInfo;
	}
	
	@Override
	public IQuestSound getSounds()
	{
		return qSounds;
	}
	
	/**
	 * Quest specific living update event. Do not use for item submissions
	 */
	@Override
	public void update(EntityPlayer player)
	{
		if(isComplete(player.getUniqueID()))
		{
			UserEntry entry = GetUserEntry(player.getUniqueID());
			
			if(!hasClaimed(player.getUniqueID()))
			{
				if(canClaim(player))
				{
					// Quest is complete and pending claim.
					// Task logic is not required to run.
					if(qInfo.getProperty(QuestProperties.AUTO_CLAIM) && player.ticksExisted%20 == 0)
					{
						claimReward(player);
					}
					
					return;
				} else if(qInfo.getProperty(QuestProperties.REPEAT_TIME).intValue() < 0 || rewards.size() <= 0)
				{
					// Task is non repeatable or has no rewards to claim
					return;
				} else
				{
					// Task logic will now run for repeat quest
				}
			} else if(rewards.size() > 0 && qInfo.getProperty(QuestProperties.REPEAT_TIME).intValue() >= 0 && player.worldObj.getTotalWorldTime() - entry.getTimestamp() >= qInfo.getProperty(QuestProperties.REPEAT_TIME).intValue())
			{
				// Task is scheduled to reset
				if(qInfo.getProperty(QuestProperties.GLOBAL))
				{
					resetAll(false);
				} else
				{
					resetUser(player.getUniqueID(), false);
				}
				
				if(!QuestSettings.INSTANCE.isEditMode() && !qInfo.getProperty(QuestProperties.SILENT))
				{
					NBTTagCompound tags = new NBTTagCompound();
					tags.setString("Main", "betterquesting.notice.update");
					tags.setString("Sub", getUnlocalisedName());
					tags.setString("Sound", qSounds.getUpdateSound());
					tags.setTag("Icon", getItemIcon().writeToNBT(new NBTTagCompound()));
					PreparedPayload payload = new PreparedPayload(PacketTypeNative.NOTIFICATION.GetLocation(), tags);
					
					if(qInfo.getProperty(QuestProperties.GLOBAL))
					{
						PacketSender.INSTANCE.sendToAll(payload);
					} else if(player instanceof EntityPlayerMP)
					{
						PacketSender.INSTANCE.sendToPlayer(payload, (EntityPlayerMP)player);
					}
				}
				
				PacketSender.INSTANCE.sendToAll(getSyncPacket());
				return;
			} else
			{
				// No reset or reset is pending
				return;
			}
		}
		
		if(isUnlocked(player.getUniqueID()) || qInfo.getProperty(QuestProperties.LOCKED_PROGRESS))
		{
			int done = 0;
			boolean update = false;
			
			for(ITaskBase tsk : tasks.getAllValues())
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
					PacketSender.INSTANCE.sendToAll(getSyncPacket());
				}
				
				return;
			} else if((tasks.size() > 0 || !QuestSettings.INSTANCE.isEditMode()) && qInfo.getProperty(QuestProperties.LOGIC_TASK).GetResult(done, tasks.size()))
			{
				setComplete(player.getUniqueID(), player.worldObj.getTotalWorldTime());
				
				PacketSender.INSTANCE.sendToAll(getSyncPacket());
				
				if(!QuestSettings.INSTANCE.isEditMode() && !qInfo.getProperty(QuestProperties.SILENT))
				{
					NBTTagCompound tags = new NBTTagCompound();
					tags.setString("Main", "betterquesting.notice.complete");
					tags.setString("Sub", getUnlocalisedName());
					tags.setString("Sound", qSounds.getCompleteSound());
					tags.setTag("Icon", getItemIcon().writeToNBT(new NBTTagCompound()));
					PreparedPayload payload = new PreparedPayload(PacketTypeNative.NOTIFICATION.GetLocation(), tags);
					
					if(qInfo.getProperty(QuestProperties.GLOBAL))
					{
						PacketSender.INSTANCE.sendToAll(payload);
					} else if(player instanceof EntityPlayerMP)
					{
						PacketSender.INSTANCE.sendToPlayer(payload, (EntityPlayerMP)player);
					}
				}
			} else if(update && qInfo.getProperty(QuestProperties.SIMULTANEOUS))
			{
				resetUser(player.getUniqueID(), false);
				PacketSender.INSTANCE.sendToAll(getSyncPacket());
			} else if(update)
			{
				PacketSender.INSTANCE.sendToAll(getSyncPacket());
				
				if(!QuestSettings.INSTANCE.isEditMode() && !qInfo.getProperty(QuestProperties.SILENT))
				{
					NBTTagCompound tags = new NBTTagCompound();
					tags.setString("Main", "betterquesting.notice.update");
					tags.setString("Sub", getUnlocalisedName());
					tags.setString("Sound", qSounds.getUpdateSound());
					tags.setTag("Icon", getItemIcon().writeToNBT(new NBTTagCompound()));
					PreparedPayload payload = new PreparedPayload(PacketTypeNative.NOTIFICATION.GetLocation(), tags);
					
					if(qInfo.getProperty(QuestProperties.GLOBAL))
					{
						PacketSender.INSTANCE.sendToAll(payload);
					} else if(player instanceof EntityPlayerMP)
					{
						PacketSender.INSTANCE.sendToPlayer(payload, (EntityPlayerMP)player);
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
		if(isComplete(player.getUniqueID()) && (qInfo.getProperty(QuestProperties.REPEAT_TIME).intValue() < 0 || rewards.size() <= 0))
		{
			return;
		} else if(!canSubmit(player))
		{
			return;
		}
		
		if(isUnlocked(player.getUniqueID()) || QuestSettings.INSTANCE.isEditMode())
		{
			int done = 0;
			boolean update = false;
			
			for(ITaskBase tsk : tasks.getAllValues())
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
			
			if((tasks.size() > 0 || !QuestSettings.INSTANCE.isEditMode()) && qInfo.getProperty(QuestProperties.LOGIC_TASK).GetResult(done, tasks.size()))
			{
				setComplete(player.getUniqueID(), player.worldObj.getTotalWorldTime());
				
				if(!QuestSettings.INSTANCE.isEditMode() && !qInfo.getProperty(QuestProperties.SILENT))
				{
					NBTTagCompound tags = new NBTTagCompound();
					tags.setString("Main", "betterquesting.notice.complete");
					tags.setString("Sub", getUnlocalisedName());
					tags.setString("Sound", qSounds.getCompleteSound());
					tags.setTag("Icon", getItemIcon().writeToNBT(new NBTTagCompound()));
					PreparedPayload payload = new PreparedPayload(PacketTypeNative.NOTIFICATION.GetLocation(), tags);
					
					if(qInfo.getProperty(QuestProperties.GLOBAL))
					{
						PacketSender.INSTANCE.sendToAll(payload);
					} else if(player instanceof EntityPlayerMP)
					{
						PacketSender.INSTANCE.sendToPlayer(payload, (EntityPlayerMP)player);
					}
				}
			} else if(update && qInfo.getProperty(QuestProperties.SIMULTANEOUS))
			{
				resetUser(player.getUniqueID(), false);
				PacketSender.INSTANCE.sendToAll(getSyncPacket());
			} else if(update)
			{
				if(!QuestSettings.INSTANCE.isEditMode() && !qInfo.getProperty(QuestProperties.SILENT))
				{
					NBTTagCompound tags = new NBTTagCompound();
					tags.setString("Main", "betterquesting.notice.update");
					tags.setString("Sub", getUnlocalisedName());
					tags.setString("Sound", qSounds.getUpdateSound());
					tags.setTag("Icon", getItemIcon().writeToNBT(new NBTTagCompound()));
					PreparedPayload payload = new PreparedPayload(PacketTypeNative.NOTIFICATION.GetLocation(), tags);
					
					if(qInfo.getProperty(QuestProperties.GLOBAL))
					{
						PacketSender.INSTANCE.sendToAll(payload);
					} else if(player instanceof EntityPlayerMP)
					{
						PacketSender.INSTANCE.sendToPlayer(payload, (EntityPlayerMP)player);
					}
				}
			}
			
			PacketSender.INSTANCE.sendToAll(getSyncPacket());
		}
	}
	
	@Override
	public boolean hasClaimed(UUID uuid)
	{
		if(rewards.size() <= 0)
		{
			return true;
		}
				
		if(qInfo.getProperty(QuestProperties.GLOBAL))
		{
			if(GetParticipation(uuid) < qInfo.getProperty(QuestProperties.PARTICIPATION).floatValue())
			{
				return true;
			} else if(!qInfo.getProperty(QuestProperties.GLOBAL_SHARE))
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
		
		if(entry == null || hasClaimed(player.getUniqueID()))
		{
			return false;
		} else if(canSubmit(player))
		{
			return false;
		} else
		{
			for(IRewardBase rew : rewards.getAllValues())
			{
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
		for(IRewardBase rew : rewards.getAllValues())
		{
			rew.claimReward(player, this);
		}
		
		UserEntry entry = GetUserEntry(player.getUniqueID());
		entry.setClaimed(true, player.worldObj.getTotalWorldTime());
		
		PacketSender.INSTANCE.sendToAll(getSyncPacket());
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
			
			for(ITaskBase tsk : tasks.getAllValues())
			{
				if(tsk.isComplete(player.getUniqueID()))
				{
					done += 1;
				}
			}
			
			return !qInfo.getProperty(QuestProperties.LOGIC_TASK).GetResult(done, tasks.size());
		} else
		{
			return false;
		}
	}
	
	private float GetParticipation(UUID uuid)
	{
		if(tasks.size() <= 0)
		{
			return 0F;
		}
		
		float total = 0F;
		
		for(ITaskBase t : tasks.getAllValues())
		{
			if(t instanceof IProgression)
			{
				total += ((IProgression<?>)t).getParticipation(uuid);
			}
		}
		
		return total / tasks.size();
	}
	
	@Override
	public List<String> getTooltip(EntityPlayer player)
	{
		if(Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT))
		{
			return this.getAdvancedTooltip(player);
		} else
		{
			return this.getStandardTooltip(player);
		}
	}
	
	@SideOnly(Side.CLIENT)
	private List<String> getStandardTooltip(EntityPlayer player)
	{
		ArrayList<String> list = new ArrayList<String>();
		
		list.add(StatCollector.translateToLocalFormatted(getUnlocalisedName()));
		
		if(isComplete(player.getUniqueID()))
		{
			list.add(EnumChatFormatting.GREEN + StatCollector.translateToLocalFormatted("betterquesting.tooltip.complete"));
			
			if(!hasClaimed(player.getUniqueID()))
			{
				list.add(EnumChatFormatting.GRAY + StatCollector.translateToLocalFormatted("betterquesting.tooltip.rewards_pending"));
			} else if(qInfo.getProperty(QuestProperties.REPEAT_TIME).intValue() > 0)
			{
				long time = getRepeatSeconds(player);
				DecimalFormat df = new DecimalFormat("00");
				list.add(EnumChatFormatting.GRAY + StatCollector.translateToLocalFormatted("betterquesting.tooltip.repeat", (time/60) + "m " + df.format(time%60) + "s"));
			}
		} else if(!isUnlocked(player.getUniqueID()))
		{
			list.add(EnumChatFormatting.RED + "" + EnumChatFormatting.UNDERLINE + StatCollector.translateToLocalFormatted("betterquesting.tooltip.requires") + " (" + qInfo.getProperty(QuestProperties.LOGIC_QUEST).toString().toUpperCase() + ")");
			
			for(IQuestContainer req : preRequisites)
			{
				if(!req.isComplete(player.getUniqueID()))
				{
					list.add(EnumChatFormatting.RED + "- " + StatCollector.translateToLocalFormatted(req.getUnlocalisedName()));
				}
			}
		} else
		{
			int n = 0;
			
			for(ITaskBase task : tasks.getAllValues())
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
	private List<String> getAdvancedTooltip(EntityPlayer player)
	{
		ArrayList<String> list = new ArrayList<String>();
		
		list.add(StatCollector.translateToLocalFormatted(getUnlocalisedName()) + " #" + QuestDatabase.INSTANCE.getKey(this));
		
		list.add(EnumChatFormatting.GRAY + StatCollector.translateToLocalFormatted("betterquesting.tooltip.main_quest", qInfo.getProperty(QuestProperties.MAIN)));
		list.add(EnumChatFormatting.GRAY + StatCollector.translateToLocalFormatted("betterquesting.tooltip.global_quest", qInfo.getProperty(QuestProperties.GLOBAL)));
		if(qInfo.getProperty(QuestProperties.GLOBAL))
		{
			list.add(EnumChatFormatting.GRAY + StatCollector.translateToLocalFormatted("betterquesting.tooltip.global_share", qInfo.getProperty(QuestProperties.GLOBAL_SHARE)));
		}
		list.add(EnumChatFormatting.GRAY + StatCollector.translateToLocalFormatted("betterquesting.tooltip.task_logic", qInfo.getProperty(QuestProperties.LOGIC_QUEST).toString().toUpperCase()));
		list.add(EnumChatFormatting.GRAY + StatCollector.translateToLocalFormatted("betterquesting.tooltip.simultaneous", qInfo.getProperty(QuestProperties.SIMULTANEOUS)));
		list.add(EnumChatFormatting.GRAY + StatCollector.translateToLocalFormatted("betterquesting.tooltip.auto_claim", qInfo.getProperty(QuestProperties.AUTO_CLAIM)));
		if(qInfo.getProperty(QuestProperties.REPEAT_TIME).intValue() >= 0)
		{
			DecimalFormat df = new DecimalFormat("00");
			list.add(EnumChatFormatting.GRAY + StatCollector.translateToLocalFormatted("betterquesting.tooltip.repeat", (qInfo.getProperty(QuestProperties.REPEAT_TIME).intValue()/60) + "m " + df.format(qInfo.getProperty(QuestProperties.REPEAT_TIME).intValue()%60) + "s"));
		} else
		{
			list.add(EnumChatFormatting.GRAY + StatCollector.translateToLocalFormatted("betterquesting.tooltip.repeat", false));
		}
		
		return list;
	}
	
	@SideOnly(Side.CLIENT)
	public long getRepeatSeconds(EntityPlayer player)
	{
		if(qInfo.getProperty(QuestProperties.REPEAT_TIME).intValue() < 0)
		{
			return -1;
		}
		
		UserEntry ue = GetUserEntry(player.getUniqueID());
		
		if(ue == null)
		{
			return 0;
		} else
		{
			return (qInfo.getProperty(QuestProperties.REPEAT_TIME).intValue() - (player.worldObj.getTotalWorldTime() - ue.getTimestamp()))/20L;
		}
	}
	
	@Override
	public PreparedPayload getSyncPacket()
	{
		NBTTagCompound tags = new NBTTagCompound();
		JsonObject base = new JsonObject();
		base.add("config", writeToJson(new JsonObject(), EnumSaveType.CONFIG));
		base.add("progress", writeToJson(new JsonObject(), EnumSaveType.PROGRESS));
		tags.setTag("data", NBTConverter.JSONtoNBT_Object(base, new NBTTagCompound()));
		tags.setInteger("questID", QuestDatabase.INSTANCE.getKey(this));
		
		return new PreparedPayload(PacketTypeNative.QUEST_SYNC.GetLocation(), tags);
	}
	
	@Override
	public void readPacket(NBTTagCompound payload)
	{
		JsonObject base = NBTConverter.NBTtoJSON_Compound(payload.getCompoundTag("data"), new JsonObject());
		
		readFromJson(JsonHelper.GetObject(base, "config"), EnumSaveType.CONFIG);
		readFromJson(JsonHelper.GetObject(base, "progress"), EnumSaveType.PROGRESS);
	}
	
	public boolean isUnlocked(UUID uuid)
	{
		int A = 0;
		int B = preRequisites.size();
		
		if(B <= 0)
		{
			return true;
		}
		
		for(IQuestContainer quest : preRequisites)
		{
			if(quest != null && quest.isComplete(uuid))
			{
				A++;
			}
		}
		
		return qInfo.getProperty(QuestProperties.LOGIC_QUEST).GetResult(A, B);
	}
	
	@Override
	public void setComplete(UUID uuid, long timestamp)
	{
		IParty party = PartyManager.INSTANCE.getUserParty(uuid);
		
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
			for(UUID mem : party.getMembers())
			{
				UserEntry entry = this.GetUserEntry(mem);
				
				if(entry != null)
				{
					entry.setClaimed(false, timestamp);
				} else
				{
					completeUsers.add(new UserEntry(mem, timestamp));
				}
			}
		}
	}
	
	/**
	 * Returns true if the quest has been completed at least once
	 */
	@Override
	public boolean isComplete(UUID uuid)
	{
		if(qInfo.getProperty(QuestProperties.GLOBAL))
		{
			return completeUsers.size() > 0;
		} else
		{
			return GetUserEntry(uuid) != null;
		}
	}
	
	private void RemoveUserEntry(UUID... uuid)
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
			PacketSender.INSTANCE.sendToAll(getSyncPacket());
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
	
	private UserEntry GetUserEntry(UUID uuid)
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
	 * Resets task progress and claim status. If performing a full reset, completion status will also be erased
	 */
	@Override
	public void resetUser(UUID uuid, boolean fullReset)
	{
		if(fullReset)
		{
			this.RemoveUserEntry(uuid);
		} else
		{
			UserEntry entry = GetUserEntry(uuid);
			
			if(entry != null)
			{
				entry.setClaimed(false, 0);
			}
		}
		
		for(ITaskBase t : tasks.getAllValues())
		{
			t.resetUser(uuid);
		}
	}
	
	/**
	 * Resets task progress and claim status for all users
	 */
	@Override
	public void resetAll(boolean fullReset)
	{
		if(fullReset)
		{
			completeUsers.clear();
		} else
		{
			for(UserEntry entry : completeUsers)
			{
				entry.setClaimed(false, 0);
			}
		}
		
		for(ITaskBase t : tasks.getAllValues())
		{
			t.resetAll();
		}
	}
	
	@Override
	public IRegStorage<ITaskBase> getTasks()
	{
		return tasks;
	}
	
	@Override
	public IRegStorage<IRewardBase> getRewards()
	{
		return rewards;
	}
	
	@Override
	public List<IQuestContainer> getPrerequisites()
	{
		return preRequisites;
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
		jObj.add("properties", qInfo.writeToJson(new JsonObject(), EnumSaveType.CONFIG));
		jObj.add("sounds", qSounds.writeToJson(new JsonObject(), EnumSaveType.CONFIG));
		jObj.add("tasks", tasks.writeToJson(new JsonArray(), EnumSaveType.CONFIG));
		jObj.add("rewards", rewards.writeToJson(new JsonArray(), EnumSaveType.CONFIG));
		
		JsonArray reqJson = new JsonArray();
		for(IQuestContainer quest : preRequisites)
		{
			reqJson.add(new JsonPrimitive(QuestDatabase.INSTANCE.getKey(quest)));
		}
		jObj.add("preRequisites", reqJson);
		
		return jObj;
	}
	
	private void readFromJson_Config(JsonObject jObj)
	{
		this.qInfo.readFromJson(JsonHelper.GetObject(jObj, "properties"), EnumSaveType.CONFIG);
		this.qSounds.readFromJson(JsonHelper.GetObject(jObj, "sounds"), EnumSaveType.CONFIG);
		this.tasks.readFromJson(JsonHelper.GetArray(jObj, "tasks"), EnumSaveType.CONFIG);
		this.rewards.readFromJson(JsonHelper.GetArray(jObj, "rewards"), EnumSaveType.CONFIG);
		
		preRequisites.clear();
		for(JsonElement entry : JsonHelper.GetArray(jObj, "preRequisites"))
		{
			if(entry == null || !entry.isJsonPrimitive() || !entry.getAsJsonPrimitive().isNumber())
			{
				continue;
			}
			
			IQuestContainer tmp = QuestDatabase.INSTANCE.getValue(entry.getAsInt());
			
			if(tmp == null)
			{
				tmp = new QuestInstance();
				QuestDatabase.INSTANCE.add(tmp, entry.getAsInt());
			}
			
			preRequisites.add(tmp);
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
	
	private void readFromJson_Progress(JsonObject json)
	{
		completeUsers.clear();
		for(JsonElement entry : JsonHelper.GetArray(json, "completed"))
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
