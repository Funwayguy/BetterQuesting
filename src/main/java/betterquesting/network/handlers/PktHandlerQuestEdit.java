package betterquesting.network.handlers;

import betterquesting.api.api.QuestingAPI;
import betterquesting.api.enums.EnumPacketAction;
import betterquesting.api.network.IPacketHandler;
import betterquesting.api.properties.NativeProps;
import betterquesting.api.questing.IQuest;
import betterquesting.api.questing.tasks.ITask;
import betterquesting.api2.storage.DBEntry;
import betterquesting.core.BetterQuesting;
import betterquesting.network.PacketSender;
import betterquesting.network.PacketTypeNative;
import betterquesting.questing.QuestDatabase;
import betterquesting.questing.QuestInstance;
import betterquesting.questing.QuestLineDatabase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;
import org.apache.logging.log4j.Level;

import java.util.UUID;

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
		if(sender == null || sender.mcServer == null)
		{
			return;
		}
		
		boolean isOP = sender.mcServer.getConfigurationManager().func_152596_g(sender.getGameProfile());
		
		if(!isOP)
		{
			BetterQuesting.logger.log(Level.WARN, "Player " + sender.getCommandSenderName() + " (UUID:" + QuestingAPI.getQuestingUUID(sender) + ") tried to edit quest without OP permissions!");
			sender.addChatComponentMessage(new ChatComponentText(EnumChatFormatting.RED + "You need to be OP to edit quests!"));
			return; // Player is not operator. Do nothing
		}
		
		int aID = !data.hasKey("action")? -1 : data.getInteger("action");
		int qID = !data.hasKey("questID")? -1 : data.getInteger("questID");
		IQuest quest = QuestDatabase.INSTANCE.getValue(qID);
		
		EnumPacketAction action;
		
		if(aID < 0 || aID >= EnumPacketAction.values().length)
		{
			return;
		}
		
		action = EnumPacketAction.values()[aID];
		
		if(action == EnumPacketAction.EDIT && quest != null)
		{
			quest.readPacket(data);
			PacketSender.INSTANCE.sendToAll(quest.getSyncPacket());
		} else if(action == EnumPacketAction.REMOVE)
		{
		    int[] bulkIDs;
		    
		    if(data.hasKey("bulkIDs"))
            {
                bulkIDs = data.getIntArray("bulkIDs");
            } else
            {
                bulkIDs = new int[]{qID};
            }
            
            for(int bid : bulkIDs)
            {
                if(bid < 0)
                {
                    BetterQuesting.logger.log(Level.ERROR, sender.getCommandSenderName() + " tried to delete non-existent quest with ID:" + bid);
                    return;
                }
    
                BetterQuesting.logger.log(Level.INFO, "Player " + sender.getCommandSenderName() + " deleted quest (#" + bid + ")");
                QuestDatabase.INSTANCE.removeID(bid);
                QuestLineDatabase.INSTANCE.removeQuest(bid);
            }
            
			PacketSender.INSTANCE.sendToAll(QuestDatabase.INSTANCE.getSyncPacket());
			PacketSender.INSTANCE.sendToAll(QuestLineDatabase.INSTANCE.getSyncPacket());
		} else if(action == EnumPacketAction.SET && quest != null) // Force Complete/Reset
		{
			if(data.getBoolean("state"))
			{
				UUID senderID = QuestingAPI.getQuestingUUID(sender);
				boolean com = quest.isComplete(senderID);
				
				if(com && quest instanceof QuestInstance)
				{
					quest.setClaimed(senderID, 0);
				} else
				{
					quest.setComplete(senderID, 0);
					
					int done = 0;
					
					if(!quest.getProperty(NativeProps.LOGIC_TASK).getResult(done, quest.getTasks().size())) // Preliminary check
					{
						for(DBEntry<ITask> entry : quest.getTasks().getEntries())
						{
							entry.getValue().setComplete(senderID);
							done += 1;
							
							if(quest.getProperty(NativeProps.LOGIC_TASK).getResult(done, quest.getTasks().size()))
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
			
			PacketSender.INSTANCE.sendToAll(quest.getSyncPacket());
		} else if(action == EnumPacketAction.ADD)
		{
			IQuest nq;
            int nID;
            
            if(!data.hasKey("questID", 99))
            {
                nID = QuestDatabase.INSTANCE.nextID();
                nq = QuestDatabase.INSTANCE.createNew(nID);
                System.out.println("Added with new ID " + nID);
            } else
            {
                nID = data.getInteger("questID");
                nq = QuestDatabase.INSTANCE.getValue(nID);
                if(nq == null) nq = QuestDatabase.INSTANCE.createNew(nID);
            }
			
			if(data.hasKey("data", 10))
			{
				nq.readFromNBT(data.getCompoundTag("data").getCompoundTag("config"));
			}
			
			PacketSender.INSTANCE.sendToAll(nq.getSyncPacket());
		}
	}

	@Override
	public void handleClient(NBTTagCompound data)
	{
	}
}
