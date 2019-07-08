package betterquesting.network.handlers;

import betterquesting.api.api.QuestingAPI;
import betterquesting.api.enums.EnumPacketAction;
import betterquesting.api.network.IPacketHandler;
import betterquesting.api.network.QuestingPacket;
import betterquesting.api.properties.NativeProps;
import betterquesting.api.questing.IQuest;
import betterquesting.api.questing.tasks.ITask;
import betterquesting.api2.storage.DBEntry;
import betterquesting.core.BetterQuesting;
import betterquesting.network.PacketSender;
import betterquesting.network.PacketTypeNative;
import betterquesting.questing.QuestDatabase;
import betterquesting.questing.QuestLineDatabase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import org.apache.logging.log4j.Level;

import java.util.UUID;

public class PktHandlerQuestEdit implements IPacketHandler
{
    public static final PktHandlerQuestEdit INSTANCE = new PktHandlerQuestEdit();
    
	@Override
	public ResourceLocation getRegistryName()
	{
		return PacketTypeNative.QUEST_EDIT.GetLocation();
	}
	
	@Override
	public void handleServer(NBTTagCompound data, EntityPlayerMP sender)
	{
		if(sender == null || sender.getServer() == null) return;
		
		boolean isOP = sender.getServer().getPlayerList().canSendCommands(sender.getGameProfile());
		
		if(!isOP)
		{
			BetterQuesting.logger.log(Level.WARN, "Player " + sender.getName() + " (UUID:" + QuestingAPI.getQuestingUUID(sender) + ") tried to edit quest without OP permissions!");
			sender.sendStatusMessage(new TextComponentString(TextFormatting.RED + "You need to be OP to edit quests!"), false);
			return; // Player is not operator. Do nothing
		}
		
		int aID = !data.hasKey("action")? -1 : data.getInteger("action");
		int qID = !data.hasKey("questID")? -1 : data.getInteger("questID");
		
		if(aID < 0 || aID >= EnumPacketAction.values().length) return;
		
		IQuest quest = QuestDatabase.INSTANCE.getValue(qID);
		EnumPacketAction action = EnumPacketAction.values()[aID];
		
		if(action == EnumPacketAction.EDIT && quest != null)
		{
		    NBTTagCompound qData = data.getCompoundTag("data");
		    if(qData.hasKey("config", 10)) quest.readFromNBT(qData.getCompoundTag("config"));
		    PktHandlerQuestSync.INSTANCE.resyncAll(new DBEntry<>(aID, quest));
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
                    BetterQuesting.logger.log(Level.ERROR, sender.getName() + " tried to delete non-existent quest with ID:" + bid);
                    continue;
                }
    
                BetterQuesting.logger.log(Level.INFO, "Player " + sender.getName() + " deleted quest (#" + bid + ")");
                QuestDatabase.INSTANCE.removeID(bid);
                QuestLineDatabase.INSTANCE.removeQuest(bid);
            }
            
            NBTTagCompound response = new NBTTagCompound();
            response.setIntArray("removeIDs", bulkIDs);
            response.setInteger("action", EnumPacketAction.REMOVE.ordinal());
			PacketSender.INSTANCE.sendToAll(new QuestingPacket(PacketTypeNative.QUEST_EDIT.GetLocation(), response)); // Much better than sending the entire database
		} else if(action == EnumPacketAction.SET && quest != null) // Force Complete/Reset
		{
			if(data.getBoolean("state"))
			{
				UUID senderID = QuestingAPI.getQuestingUUID(sender);
				
				if(quest.isComplete(senderID))
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
			
			PktHandlerQuestSync.INSTANCE.resyncAll(new DBEntry<>(qID, quest));
		} else if(action == EnumPacketAction.ADD)
		{
			IQuest nq;
            int nID;
            
            if(!data.hasKey("questID", 99))
            {
                nID = QuestDatabase.INSTANCE.nextID();
                nq = QuestDatabase.INSTANCE.createNew(nID);
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
			
			PktHandlerQuestSync.INSTANCE.resyncAll(new DBEntry<>(nID, nq));
		}
	}

	@Override
	public void handleClient(NBTTagCompound data)
	{
	    int aID = !data.hasKey("action")? -1 : data.getInteger("action");
		
		if(aID < 0 || aID >= EnumPacketAction.values().length) return;
		EnumPacketAction action = EnumPacketAction.values()[aID];
		
		if(action == EnumPacketAction.REMOVE)
        {
            for(int id : data.getIntArray("removeIDs"))
            {
                QuestDatabase.INSTANCE.removeID(id);
                QuestLineDatabase.INSTANCE.removeQuest(id);
            }
        }
	}
}
