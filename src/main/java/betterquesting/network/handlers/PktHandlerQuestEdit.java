package betterquesting.network.handlers;

import java.util.UUID;

import betterquesting.api2.storage.DBEntry;
import betterquesting.questing.QuestLineDatabase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import org.apache.logging.log4j.Level;
import betterquesting.api.api.QuestingAPI;
import betterquesting.api.enums.EnumPacketAction;
import betterquesting.api.enums.EnumSaveType;
import betterquesting.api.network.IPacketHandler;
import betterquesting.api.properties.NativeProps;
import betterquesting.api.questing.IQuest;
import betterquesting.api.questing.tasks.ITask;
import betterquesting.core.BetterQuesting;
import betterquesting.network.PacketSender;
import betterquesting.network.PacketTypeNative;
import betterquesting.questing.QuestDatabase;
import betterquesting.questing.QuestInstance;

public class PktHandlerQuestEdit implements IPacketHandler
{
	@Override
	public ResourceLocation getRegistryName()
	{
		return PacketTypeNative.QUEST_EDIT.GetLocation();
	}
	
	@Override
	public void handleServer(NBTTagCompound data, EntityPlayerMP sender)
	{
		if(sender == null)
		{
			return;
		}
		
		boolean isOP = sender.world.getMinecraftServer().getPlayerList().canSendCommands(sender.getGameProfile());
		
		if(!isOP)
		{
			BetterQuesting.logger.log(Level.WARN, "Player " + sender.getName() + " (UUID:" + QuestingAPI.getQuestingUUID(sender) + ") tried to edit quest without OP permissions!");
			sender.sendStatusMessage(new TextComponentString(TextFormatting.RED + "You need to be OP to edit quests!"), false);
			return; // Player is not operator. Do nothing
		}
		
		int aID = !data.hasKey("action")? -1 : data.getInteger("action");
		int qID = !data.hasKey("questID")? -1 : data.getInteger("questID");
		IQuest quest = QuestDatabase.INSTANCE.getValue(qID);
		
		EnumPacketAction action = null;
		
		if(aID < 0 || aID >= EnumPacketAction.values().length)
		{
			return;
		}
		
		action = EnumPacketAction.values()[aID];
		
		if(action == EnumPacketAction.EDIT && quest != null)
		{
			NBTTagCompound base = data.getCompoundTag("data");
			if (base.hasKey("progress"))
			{
				// Clients never tell the server what's the current progress
				base.removeTag("progress");
			}
			
			quest.readPacket(data);
			quest.notifyAllOnlineOfConfigChange();
			return;
		} else if(action == EnumPacketAction.REMOVE)
		{
			if(quest == null || qID < 0)
			{
				BetterQuesting.logger.log(Level.ERROR, sender.getName() + " tried to delete non-existent quest with ID:" + qID);
				return;
			}
			
			BetterQuesting.logger.log(Level.INFO, "Player " + sender.getName() + " deleted quest " + quest.getUnlocalisedName());
			QuestDatabase.INSTANCE.removeID(qID);
			QuestLineDatabase.INSTANCE.removeQuest(qID);
			PacketSender.INSTANCE.sendToAll(QuestDatabase.INSTANCE.getSyncPacket());
			PacketSender.INSTANCE.sendToAll(QuestLineDatabase.INSTANCE.getSyncPacket());
			return;
		} else if(action == EnumPacketAction.SET && quest != null) // Force Complete/Reset
		{
			if(data.getBoolean("state"))
			{
				UUID senderID = QuestingAPI.getQuestingUUID(sender);
				boolean com = quest.isComplete(senderID);
				
				if(com && quest instanceof QuestInstance)
				{
					((QuestInstance)quest).setClaimed(senderID, 0);
				} else
				{
					quest.setComplete(senderID, 0);
					
					int done = 0;
					
					if(!quest.getProperties().getProperty(NativeProps.LOGIC_TASK).getResult(done, quest.getTasks().size())) // Preliminary check
					{
						for(DBEntry<ITask> entry : quest.getTasks().getEntries())
						{
							entry.getValue().setComplete(senderID);
							done += 1;
							
							if(quest.getProperties().getProperty(NativeProps.LOGIC_TASK).getResult(done, quest.getTasks().size()))
							{
								break; // Only complete enough quests to claim the reward
							}
						}
					}
				}
			} else
			{
				quest.resetAll(true);
			}
			
			quest.notifyAllOnlineOfConfigChange();
			return;
		} else if(action == EnumPacketAction.ADD)
		{
			IQuest nq = new QuestInstance();
			//nq.setParentDatabase(QuestDatabase.INSTANCE);
			int nID = QuestDatabase.INSTANCE.nextID();
			
			if(data.hasKey("data") && data.hasKey("questID"))
			{
				nID = data.getInteger("questID");
				NBTTagCompound base = data.getCompoundTag("data");
				
				nq.readFromNBT(base.getCompoundTag("config"), EnumSaveType.CONFIG);
			}
			
			QuestDatabase.INSTANCE.add(nID, nq);
			PacketSender.INSTANCE.sendToAll(nq.getSyncPacket());
			return;
		}
	}

	@Override
	public void handleClient(NBTTagCompound data)
	{
		return;
	}
}
