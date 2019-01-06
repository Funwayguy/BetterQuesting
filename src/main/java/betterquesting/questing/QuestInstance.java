package betterquesting.questing;

import betterquesting.api.api.ApiReference;
import betterquesting.api.api.QuestingAPI;
import betterquesting.api.enums.EnumLogic;
import betterquesting.api.enums.EnumPartyStatus;
import betterquesting.api.enums.EnumQuestState;
import betterquesting.api.network.QuestingPacket;
import betterquesting.api.properties.IPropertyType;
import betterquesting.api.properties.NativeProps;
import betterquesting.api.questing.IQuest;
import betterquesting.api.questing.IQuestDatabase;
import betterquesting.api.questing.party.IParty;
import betterquesting.api.questing.rewards.IReward;
import betterquesting.api.questing.tasks.IProgression;
import betterquesting.api.questing.tasks.ITask;
import betterquesting.api.utils.BigItemStack;
import betterquesting.api2.storage.DBEntry;
import betterquesting.api2.storage.IDatabaseNBT;
import betterquesting.api2.utils.QuestTranslation;
import betterquesting.core.BetterQuesting;
import betterquesting.misc.UserEntry;
import betterquesting.network.PacketSender;
import betterquesting.network.PacketTypeNative;
import betterquesting.questing.party.PartyManager;
import betterquesting.questing.rewards.RewardStorage;
import betterquesting.questing.tasks.TaskStorage;
import betterquesting.storage.PropertyContainer;
import betterquesting.storage.QuestSettings;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.nbt.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.Level;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

public class QuestInstance implements IQuest
{
    // TODO: MAKE THIS ALL THREAD SAFE PLEAAAAAASE
    
	private final TaskStorage tasks = new TaskStorage();
	private final RewardStorage rewards = new RewardStorage();
	
	private final List<UserEntry> completeUsers = new CopyOnWriteArrayList<>();
	// TODO: Change this to IDs. Keeping references to the objects hinders garbage collection and requires whole databases to be rewritten when a requisite is deleted.
    // TODO: A broadcasted event will need to be fired to clean unused IDs when a quest is deleted however it does not have to save to NBT/disk when doing so.
    // NOTE: IDs are much faster to read/write to NBT because we don't require database lookups to convert to/from objects.
	private final List<IQuest> preRequisites = new CopyOnWriteArrayList<>();
	
	private PropertyContainer qInfo = new PropertyContainer();
	
	private IQuestDatabase parentDB;
	
	public QuestInstance()
	{
		parentDB = QuestingAPI.getAPI(ApiReference.QUEST_DB);
		
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
		setupValue(NativeProps.LOCKED_PROGRESS, false);
		setupValue(NativeProps.AUTO_CLAIM, false);
		setupValue(NativeProps.SILENT, false);
		setupValue(NativeProps.MAIN, false);
		setupValue(NativeProps.PARTY_LOOT, false);
		setupValue(NativeProps.GLOBAL_SHARE, false);
		setupValue(NativeProps.SIMULTANEOUS, false);
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
	public void setParentDatabase(IQuestDatabase questDB)
	{
		this.parentDB = questDB;
	}
	
	/**
	 * Quest specific living update event. Do not use for item submissions
	 */
	@Override
	public void update(EntityPlayer player)
	{
		UUID playerID = QuestingAPI.getQuestingUUID(player);
		
		if(isComplete(playerID))
		{
			UserEntry entry = getCompletionInfo(playerID);
			
			if(!hasClaimed(playerID))
			{
				if(canClaim(player))
				{
					// Quest is complete and pending claim.
					// Task logic is not required to run.
					if(qInfo.getProperty(NativeProps.AUTO_CLAIM) && player.ticksExisted%20 == 0)
					{
						claimReward(player);
					}
					
					return;
				} else if(qInfo.getProperty(NativeProps.REPEAT_TIME) < 0 || rewards.size() <= 0)
				{
					// Task is non repeatable or has no rewards to claim
					return;
				}
				
				// Task logic will now run for repeat quest
			} else if(rewards.size() > 0 && qInfo.getProperty(NativeProps.REPEAT_TIME) >= 0 && FMLCommonHandler.instance().getMinecraftServerInstance().getWorld(0).getTotalWorldTime() - entry.getTimestamp() >= qInfo.getProperty(NativeProps.REPEAT_TIME))
			{
				// Task is scheduled to reset
				if(qInfo.getProperty(NativeProps.GLOBAL))
				{
					resetAll(false);
				} else
				{
					resetUser(playerID, false);
				}
				
				if(!QuestSettings.INSTANCE.getProperty(NativeProps.EDIT_MODE) && !qInfo.getProperty(NativeProps.SILENT))
				{
					postPresetNotice(player, 1);
				}
				
				PacketSender.INSTANCE.sendToAll(getSyncPacket());
				return;
			} else
			{
				// No reset or reset is pending
				return;
			}
		}
		
		if(isUnlocked(playerID) || qInfo.getProperty(NativeProps.LOCKED_PROGRESS))
		{
			int done = 0;
			
			for(DBEntry<ITask> entry : tasks.getEntries())
			{
				if(entry.getValue().isComplete(playerID))
				{
					IParty party = PartyManager.INSTANCE.getUserParty(playerID);
					
					if(party != null) // Ensures task is marked as complete for all team members
					{
						for(UUID mem : party.getMembers())
						{
							entry.getValue().setComplete(mem);
						}
					}
					
					done += 1;
				}
			}
			
			if(!isUnlocked(playerID))
			{
				return;
			} else if(!isComplete(playerID) && (tasks.size() > 0 || !QuestSettings.INSTANCE.getProperty(NativeProps.EDIT_MODE)) && qInfo.getProperty(NativeProps.LOGIC_TASK).getResult(done, tasks.size()))
			{
				setComplete(playerID, FMLCommonHandler.instance().getMinecraftServerInstance().getWorld(0).getTotalWorldTime());
				
				PacketSender.INSTANCE.sendToAll(getSyncPacket());
				
				if(!QuestSettings.INSTANCE.getProperty(NativeProps.EDIT_MODE) && !qInfo.getProperty(NativeProps.SILENT))
				{
					postPresetNotice(player, 2);
				}
			} else if(!isComplete(playerID) && done > 0 && qInfo.getProperty(NativeProps.SIMULTANEOUS))
			{
				resetUser(playerID, false);
				PacketSender.INSTANCE.sendToAll(getSyncPacket());
			}
		}
	}
	
	/**
	 * Fired when someone clicks the detect button for this quest
	 */
	@Override
	public void detect(EntityPlayer player)
	{
		UUID playerID = QuestingAPI.getQuestingUUID(player);
		
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
			
			for(DBEntry<ITask> entry : tasks.getEntries())
			{
				if(!entry.getValue().isComplete(playerID))
				{
					entry.getValue().detect(player, this);
					
					if(entry.getValue().isComplete(playerID))
					{
						IParty party = PartyManager.INSTANCE.getUserParty(playerID);
						
						if(party != null) // Ensures task is marked as complete for all team members
						{
							for(UUID mem : party.getMembers())
							{
								entry.getValue().setComplete(mem);
							}
						}
						
						done += 1;
						update = true;
					}
				} else
				{
					done += 1;
				}
			}
			
			if(tasks.size() <= 0 || qInfo.getProperty(NativeProps.LOGIC_TASK).getResult(done, tasks.size()))
			{
				setComplete(playerID, FMLCommonHandler.instance().getMinecraftServerInstance().getWorld(0).getTotalWorldTime());
				
				if(!QuestSettings.INSTANCE.getProperty(NativeProps.EDIT_MODE) && !qInfo.getProperty(NativeProps.SILENT))
				{
					postPresetNotice(player, 2);
				}
			} else if(update && qInfo.getProperty(NativeProps.SIMULTANEOUS))
			{
				resetUser(playerID, false);
				PacketSender.INSTANCE.sendToAll(getSyncPacket());
			} else if(update)
			{
				if(!QuestSettings.INSTANCE.getProperty(NativeProps.EDIT_MODE) && !qInfo.getProperty(NativeProps.SILENT))
				{
					postPresetNotice(player, 1);
				}
			}
			
			PacketSender.INSTANCE.sendToAll(getSyncPacket());
		}
	}
	
	public void postPresetNotice(EntityPlayer player, int preset)
	{
		switch(preset)
		{
			case 0:
				postNotice(player, "betterquesting.notice.unlock", getProperty(NativeProps.NAME), getProperty(NativeProps.SOUND_UNLOCK), getProperty(NativeProps.ICON));
				break;
			case 1:
				postNotice(player, "betterquesting.notice.update", getProperty(NativeProps.NAME), getProperty(NativeProps.SOUND_UPDATE), getProperty(NativeProps.ICON));
				break;
			case 2:
				postNotice(player, "betterquesting.notice.complete", getProperty(NativeProps.NAME), getProperty(NativeProps.SOUND_COMPLETE), getProperty(NativeProps.ICON));
				break;
		}
	}
	
	public void postNotice(EntityPlayer player, String mainTxt, String subTxt, String sound, BigItemStack icon)
	{
		if(QuestDatabase.INSTANCE.getID(this) < 0)
		{
			BetterQuesting.logger.error("Non-existant quest is posting notifications!", new Exception());
		}
		
		NBTTagCompound tags = new NBTTagCompound();
		tags.setString("Main", mainTxt);
		tags.setString("Sub", subTxt);
		tags.setString("Sound", sound);
		tags.setTag("Icon", icon.writeToNBT(new NBTTagCompound()));
		QuestingPacket payload = new QuestingPacket(PacketTypeNative.NOTIFICATION.GetLocation(), tags);
		
		if(qInfo.getProperty(NativeProps.GLOBAL))
		{
			PacketSender.INSTANCE.sendToAll(payload);
		} else if(player instanceof EntityPlayerMP)
		{
			List<EntityPlayerMP> tarList = getPartyPlayers((EntityPlayerMP)player);
			
			for(EntityPlayerMP p : tarList)
			{
				PacketSender.INSTANCE.sendToPlayer(payload, p);
			}
		}
	}
	
	private List<EntityPlayerMP> getPartyPlayers(EntityPlayerMP player)
	{
		List<EntityPlayerMP> list = new ArrayList<>();
		IParty party = PartyManager.INSTANCE.getUserParty(QuestingAPI.getQuestingUUID(player));
		
		if(party == null)
		{
			list.add(player);
			return list;
		} else
		{
			MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
			
			for(UUID mem : party.getMembers())
			{
				for(EntityPlayerMP p : server.getPlayerList().getPlayers())
				{
					if(p != null && QuestingAPI.getQuestingUUID(p).equals(mem))
					{
						list.add(p);
					}
				}
			}
			
			return list;
		}
	}
	
	@Override
	public boolean hasClaimed(UUID uuid)
	{
		if(rewards.size() <= 0)
		{
			return true;
		}
  
		if(qInfo.getProperty(NativeProps.GLOBAL))
		{
			if(GetParticipation(uuid) < qInfo.getProperty(NativeProps.PARTICIPATION))
			{
				return true;
			} else if(!qInfo.getProperty(NativeProps.GLOBAL_SHARE))
			{
				for(UserEntry entry : completeUsers)
				{
					if(entry.getNbtData().getBoolean("claimed"))
					{
						return true;
					}
				}
				
				return false;
			}
		}
		
		UserEntry entry = getCompletionInfo(uuid);
		
		if(entry == null)
		{
			return false;
		}
		
		return entry.getNbtData().getBoolean("claimed");
	}
	
	@Override
	public boolean canClaim(EntityPlayer player)
	{
		UserEntry entry = getCompletionInfo(QuestingAPI.getQuestingUUID(player));
		
		if(entry == null || hasClaimed(QuestingAPI.getQuestingUUID(player)))
		{
			return false;
		} else if(canSubmit(player))
		{
			return false;
		} else
		{
			for(DBEntry<IReward> rew : rewards.getEntries())
			{
				if(!rew.getValue().canClaim(player, this))
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
		for(DBEntry<IReward> rew : rewards.getEntries())
		{
			rew.getValue().claimReward(player, this);
		}
		
		UUID pID = QuestingAPI.getQuestingUUID(player);
		IParty party = PartyManager.INSTANCE.getUserParty(pID);
		
		if(party != null && this.qInfo.getProperty(NativeProps.PARTY_LOOT))
		{
			for(UUID mem : party.getMembers())
			{
				EnumPartyStatus pStat = party.getStatus(mem);
				
				if(pStat == null || pStat == EnumPartyStatus.INVITE)
				{
					continue;
				}
				
				UserEntry entry = getCompletionInfo(mem);
				
				if(entry == null)
				{
					entry = new UserEntry(mem);
					this.completeUsers.add(entry);
				}
				
				entry.setClaimed(true, FMLCommonHandler.instance().getMinecraftServerInstance().getWorld(0).getTotalWorldTime());
			}
		} else
		{
			UserEntry entry = getCompletionInfo(pID);
			
			if(entry == null)
			{
				entry = new UserEntry(pID);
				this.completeUsers.add(entry);
			}
			
			entry.setClaimed(true, FMLCommonHandler.instance().getMinecraftServerInstance().getWorld(0).getTotalWorldTime());
		}
		
		PacketSender.INSTANCE.sendToAll(getSyncPacket());
	}
	
	@Override
	public boolean canSubmit(EntityPlayer player)
	{
		if(player == null)
		{
			return false;
		}
		
		UUID playerID = QuestingAPI.getQuestingUUID(player);
		
		UserEntry entry = this.getCompletionInfo(playerID);
		
		if(entry == null) // Incomplete
		{
			return true;
		} else if(!entry.hasClaimed() && getProperty(NativeProps.REPEAT_TIME) >= 0) // Complete but repeatable
		{
			if(tasks.size() <= 0)
			{
				return true;
			}
			
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
	
	private float GetParticipation(UUID uuid)
	{
		if(tasks.size() <= 0)
		{
			return 0F;
		}
		
		float total = 0F;
		
		for(DBEntry<ITask> t : tasks.getEntries())
		{
			if(t.getValue() instanceof IProgression)
			{
				total += ((IProgression)t.getValue()).getParticipation(uuid);
			}
		}
		
		return total / tasks.size();
	}
	
	@Override
	public List<String> getTooltip(EntityPlayer player)
	{
		List<String> tooltip = this.getStandardTooltip(player);
		
		if(Minecraft.getMinecraft().gameSettings.advancedItemTooltips)
		{
			tooltip.add("");
			tooltip.addAll(this.getAdvancedTooltip(player));
		}
		
		return tooltip;
	}
	
	@SideOnly(Side.CLIENT)
	private List<String> getStandardTooltip(EntityPlayer player)
	{
		List<String> list = new ArrayList<>();
		
		list.add(QuestTranslation.translate(qInfo.getProperty(NativeProps.NAME)) + (!Minecraft.getMinecraft().gameSettings.advancedItemTooltips ? "" : (" #" + parentDB.getID(this))));
		
		UUID playerID = QuestingAPI.getQuestingUUID(player);
		
		if(isComplete(playerID))
		{
			list.add(TextFormatting.GREEN + QuestTranslation.translate("betterquesting.tooltip.complete"));
			
			if(!hasClaimed(playerID))
			{
				list.add(TextFormatting.GRAY + QuestTranslation.translate("betterquesting.tooltip.rewards_pending"));
			} else if(qInfo.getProperty(NativeProps.REPEAT_TIME) > 0)
			{
				long time = getRepeatSeconds(player);
				DecimalFormat df = new DecimalFormat("00");
				String timeTxt = "";
				
				if(time >= 3600)
				{
					timeTxt += (time/3600) + "h " + df.format((time%3600)/60) + "m ";
				} else if(time >= 60)
				{
					timeTxt += (time/60) + "m ";
				}
				
				timeTxt += df.format(time%60) + "s";
				
				list.add(TextFormatting.GRAY + QuestTranslation.translate("betterquesting.tooltip.repeat", timeTxt));
			}
		} else if(!isUnlocked(playerID))
		{
			list.add(TextFormatting.RED + "" + TextFormatting.UNDERLINE + QuestTranslation.translate("betterquesting.tooltip.requires") + " (" + qInfo.getProperty(NativeProps.LOGIC_QUEST).toString().toUpperCase() + ")");
			
			for(IQuest req : preRequisites)
			{
				if(!req.isComplete(playerID))
				{
					list.add(TextFormatting.RED + "- " + QuestTranslation.translate(req.getProperty(NativeProps.NAME)));
				}
			}
		} else
		{
			int n = 0;
			
			for(DBEntry<ITask> task : tasks.getEntries())
			{
				if(task.getValue().isComplete(playerID))
				{
					n++;
				}
			}
			
			list.add(TextFormatting.GRAY + QuestTranslation.translate("betterquesting.tooltip.tasks_complete", n, tasks.size()));
		}
		
		return list;
	}
	
	@SideOnly(Side.CLIENT)
	private List<String> getAdvancedTooltip(EntityPlayer player)
	{
		List<String> list = new ArrayList<>();
		
		//list.add(I18n.format(getUnlocalisedName()) + " #" + parentDB.getKey(this));
		
		list.add(TextFormatting.GRAY + QuestTranslation.translate("betterquesting.tooltip.main_quest", qInfo.getProperty(NativeProps.MAIN)));
		list.add(TextFormatting.GRAY + QuestTranslation.translate("betterquesting.tooltip.global_quest", qInfo.getProperty(NativeProps.GLOBAL)));
		if(qInfo.getProperty(NativeProps.GLOBAL))
		{
			list.add(TextFormatting.GRAY + QuestTranslation.translate("betterquesting.tooltip.global_share", qInfo.getProperty(NativeProps.GLOBAL_SHARE)));
		}
		list.add(TextFormatting.GRAY + QuestTranslation.translate("betterquesting.tooltip.task_logic", qInfo.getProperty(NativeProps.LOGIC_QUEST).toString().toUpperCase()));
		list.add(TextFormatting.GRAY + QuestTranslation.translate("betterquesting.tooltip.simultaneous", qInfo.getProperty(NativeProps.SIMULTANEOUS)));
		list.add(TextFormatting.GRAY + QuestTranslation.translate("betterquesting.tooltip.auto_claim", qInfo.getProperty(NativeProps.AUTO_CLAIM)));
		if(qInfo.getProperty(NativeProps.REPEAT_TIME).intValue() >= 0)
		{
			long time = qInfo.getProperty(NativeProps.REPEAT_TIME)/20;
			DecimalFormat df = new DecimalFormat("00");
			String timeTxt = "";
			
			if(time >= 3600)
			{
				timeTxt += (time/3600) + "h " + df.format((time%3600)/60) + "m ";
			} else if(time >= 60)
			{
				timeTxt += (time/60) + "m ";
			}
			
			timeTxt += df.format(time%60) + "s";
			
			list.add(TextFormatting.GRAY + QuestTranslation.translate("betterquesting.tooltip.repeat", timeTxt));
		} else
		{
			list.add(TextFormatting.GRAY + QuestTranslation.translate("betterquesting.tooltip.repeat", false));
		}
		
		return list;
	}
	
	@SideOnly(Side.CLIENT)
	public long getRepeatSeconds(EntityPlayer player)
	{
		if(qInfo.getProperty(NativeProps.REPEAT_TIME) < 0)
		{
			return -1;
		}
		
		UserEntry ue = getCompletionInfo(QuestingAPI.getQuestingUUID(player));
		
		if(ue == null)
		{
			return 0;
		} else
		{
		    // TODO: This isn't accurate outside of the overworld dimension. Adjust later
			return (qInfo.getProperty(NativeProps.REPEAT_TIME) - (player.world.getTotalWorldTime() - ue.getTimestamp()))/20L;
		}
	}
	
	@Override
	public QuestingPacket getSyncPacket()
	{
		NBTTagCompound tags = new NBTTagCompound();
		NBTTagCompound base = new NBTTagCompound();
		base.setTag("config", writeToNBT(new NBTTagCompound()));
		base.setTag("progress", writeProgressToNBT(new NBTTagCompound(), null));
		tags.setTag("data", base);
		tags.setInteger("questID", parentDB.getID(this));
		
		return new QuestingPacket(PacketTypeNative.QUEST_SYNC.GetLocation(), tags);
	}
	
	@Override
	public void readPacket(NBTTagCompound payload)
	{
		NBTTagCompound base = payload.getCompoundTag("data");
		
		readFromNBT(base.getCompoundTag("config"));
		readProgressFromNBT(base.getCompoundTag("progress"), false);
	}
	
	public boolean isUnlocked(UUID uuid)
	{
		int A = 0;
		int B = preRequisites.size();
		
		if(B <= 0)
		{
			return true;
		}
		
		for(IQuest quest : preRequisites)
		{
			if(quest != null && quest.isComplete(uuid))
			{
				A++;
			}
		}
		
		return qInfo.getProperty(NativeProps.LOGIC_QUEST).getResult(A, B);
	}
	
	@Override
	public void setComplete(UUID uuid, long timestamp)
	{
		IParty party = PartyManager.INSTANCE.getUserParty(uuid);
		
		if(party == null)
		{
			UserEntry entry = this.getCompletionInfo(uuid);
			
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
				UserEntry entry = this.getCompletionInfo(mem);
				
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
		if(qInfo.getProperty(NativeProps.GLOBAL))
		{
			return completeUsers.size() > 0;
		} else
		{
			return getCompletionInfo(uuid) != null;
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
	
	@Override
	public UserEntry getCompletionInfo(UUID uuid)
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
	
	@Override
    public void setCompletionInfo(UUID uuid, NBTTagCompound nbt)
    {
        if(uuid == null) return;
        
        if(nbt == null)
        {
            Iterator<UserEntry> iterEntry = completeUsers.iterator();
            
            while(iterEntry.hasNext())
            {
                if(iterEntry.next().getUUID().equals(uuid))
                {
                    iterEntry.remove();
                    break;
                }
            }
        } else
        {
            UserEntry ue = new UserEntry(uuid);
            ue.readFromJson(nbt);
            completeUsers.add(ue);
        }
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
			UserEntry entry = getCompletionInfo(uuid);
			
			if(entry != null)
			{
				entry.setClaimed(false, 0);
			}
		}
		
		for(DBEntry<ITask> t : tasks.getEntries())
		{
			t.getValue().resetUser(uuid);
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
		
		for(DBEntry<ITask> t : tasks.getEntries())
		{
			t.getValue().resetAll();
		}
	}
	
	@Override
	public IDatabaseNBT<ITask, NBTTagList, NBTTagList> getTasks()
	{
		return tasks;
	}
	
	@Override
	public IDatabaseNBT<IReward, NBTTagList, NBTTagList> getRewards()
	{
		return rewards;
	}
	
	@Override
	public List<IQuest> getPrerequisites()
	{
		return preRequisites;
	}
	
	// Temporary dummy variable used to prevent IO while reading/writing
	private final Boolean syncLock = true;
	
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound jObj)
	{
		jObj.setTag("properties", qInfo.writeToNBT(new NBTTagCompound()));
		jObj.setTag("tasks", tasks.writeToNBT(new NBTTagList()));
		jObj.setTag("rewards", rewards.writeToNBT(new NBTTagList()));
		
		IQuest[] pri = preRequisites.toArray(new IQuest[0]);
		int[] reqArr = new int[preRequisites.size()];
		for(int i = 0; i < pri.length; i++)
		{
			reqArr[i] = parentDB.getID(pri[i]);
		}
		jObj.setTag("preRequisites", new NBTTagIntArray(reqArr));
		
		return jObj;
	}
	
	@Override
	public void readFromNBT(NBTTagCompound jObj)
	{
		this.qInfo.readFromNBT(jObj.getCompoundTag("properties"));
		this.tasks.readFromNBT(jObj.getTagList("tasks", 10));
		this.rewards.readFromNBT(jObj.getTagList("rewards", 10));
		
		preRequisites.clear();
		
		if(jObj.getTagId("preRequisites") == 11) // Native NBT
		{
			for(int prID : jObj.getIntArray("preRequisites"))
			{
				if(prID < 0)
				{
					continue;
				}
				
				IQuest tmp = parentDB.getValue(prID);
				
				if(tmp == null)
				{
					// TODO: Make this unnecessary and only use IDs. Seriously, it adds out-of-order loading and that's a problem.
					// Track parent-child mapping in a separate database which also holds the conditions and their data
					tmp = parentDB.createNew(prID);
				}
				
				preRequisites.add(tmp);
			}
		} else // Probably an NBTTagList
		{
			NBTTagList rList = jObj.getTagList("preRequisites", 4);
			for(int i = 0; i < rList.tagCount(); i++)
			{
				NBTBase pTag = rList.get(i);
				int prID = pTag instanceof NBTPrimitive ? ((NBTPrimitive)pTag).getInt() : -1;
				
				if(prID < 0)
				{
					continue;
				}
				
				IQuest tmp = parentDB.getValue(prID);
				
				if(tmp == null)
				{
					tmp = parentDB.createNew(prID);
				}
				
				preRequisites.add(tmp);
			}
		}
		
		this.setupProps();
	}
	
	@Override
	public NBTTagCompound writeProgressToNBT(NBTTagCompound json, List<UUID> users)
	{
		NBTTagList comJson = new NBTTagList();
		for(UserEntry entry : completeUsers)
		{
			comJson.appendTag(entry.writeToJson(new NBTTagCompound()));
		}
		json.setTag("completed", comJson);
		
		NBTTagList tskJson = tasks.writeProgressToNBT(new NBTTagList(), users);
		json.setTag("tasks", tskJson);
		
		return json;
	}
	
	@Override
	public void readProgressFromNBT(NBTTagCompound json, boolean merge)
	{
		completeUsers.clear();
		NBTTagList comList = json.getTagList("completed", 10);
		for(int i = 0; i < comList.tagCount(); i++)
		{
			NBTBase entry = comList.get(i);
			
			if(entry.getId() != 10)
			{
				continue;
			}
			
			try
			{
				NBTTagCompound eTag = (NBTTagCompound)entry;
				UUID uuid = UUID.fromString(eTag.getString("uuid"));
				UserEntry user = new UserEntry(uuid);
				user.readFromJson(eTag);
				completeUsers.add(user);
			} catch(Exception e)
			{
				BetterQuesting.logger.log(Level.ERROR, "Unable to load UUID for quest", e);
			}
		}
		
		tasks.readProgressFromNBT(json.getTagList("tasks", 10), merge);
	}
	
	/**
	 * Temporary hack to make this a thing for users
	 */
	public void setClaimed(UUID uuid, long timestamp)
	{
		IParty party = PartyManager.INSTANCE.getUserParty(uuid);
		
		if(party == null)
		{
			UserEntry entry = this.getCompletionInfo(uuid);
			
			if(entry != null)
			{
				entry.setClaimed(true, timestamp);
			} else
			{
				entry = new UserEntry(uuid, timestamp);
				entry.setClaimed(true, timestamp);
				completeUsers.add(entry);
			}
		} else
		{
			for(UUID mem : party.getMembers())
			{
				UserEntry entry = this.getCompletionInfo(mem);
				
				if(entry != null)
				{
					entry.setClaimed(true, timestamp);
				} else
				{
					entry = new UserEntry(mem, timestamp);
					entry.setClaimed(true, timestamp);
					completeUsers.add(entry);
				}
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
}
