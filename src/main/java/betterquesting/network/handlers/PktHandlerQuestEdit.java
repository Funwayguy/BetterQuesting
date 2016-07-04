package betterquesting.network.handlers;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import org.apache.logging.log4j.Level;
import betterquesting.core.BetterQuesting;
import betterquesting.quests.QuestDatabase;
import betterquesting.quests.QuestInstance;
import betterquesting.quests.tasks.TaskBase;
import betterquesting.utils.NBTConverter;
import com.google.gson.JsonObject;

public class PktHandlerQuestEdit extends PktHandler
{
	@Override
	public void handleServer(EntityPlayerMP sender, NBTTagCompound data)
	{
		if(sender == null)
		{
			return;
		}
		
		if(!MinecraftServer.getServer().getConfigurationManager().func_152596_g(sender.getGameProfile()))
		{
			BetterQuesting.logger.log(Level.WARN, "Player " + sender.getCommandSenderName() + " (UUID:" + sender.getUniqueID() + ") tried to edit quests without OP permissions!");
			sender.addChatComponentMessage(new ChatComponentText(EnumChatFormatting.RED + "You need to be OP to edit quests!"));
			return; // Player is not operator. Do nothing
		}
		
		int action = !data.hasKey("action")? -1 : data.getInteger("action");
		int qID = !data.hasKey("questID")? -1 : data.getInteger("questID");
		QuestInstance quest = QuestDatabase.getQuestByID(qID);
		
		if(action < 0)
		{
			BetterQuesting.logger.log(Level.ERROR, sender.getCommandSenderName() + " tried to perform invalid quest edit action: " + action);
			return;
		}
		
		if(action == 0) // Update quest data
		{
			if(quest == null || qID < 0)
			{
				BetterQuesting.logger.log(Level.ERROR, sender.getCommandSenderName() + " tried to edit non-existent quest with ID:" + qID);
				return;
			}
			
			int ps = quest.preRequisites.size();
			
			BetterQuesting.logger.log(Level.INFO, "Player " + sender.getCommandSenderName() + " edited quest " + quest.name);
			JsonObject json1 = NBTConverter.NBTtoJSON_Compound(data.getCompoundTag("Data"), new JsonObject());
			quest.readFromJSON(json1);
			JsonObject json2 = NBTConverter.NBTtoJSON_Compound(data.getCompoundTag("Progress"), new JsonObject());
			quest.readProgressFromJSON(json2);
			
			if(ps != quest.preRequisites.size())
			{
				QuestDatabase.UpdateClients();
			} else
			{
				quest.UpdateClients();
			}
		} else if(action == 1) // Delete quest
		{
			if(quest == null || qID < 0)
			{
				BetterQuesting.logger.log(Level.ERROR, sender.getCommandSenderName() + " tried to delete non-existent quest with ID:" + qID);
				return;
			}
			
			BetterQuesting.logger.log(Level.INFO, "Player " + sender.getCommandSenderName() + " deleted quest " + quest.name);
			QuestDatabase.DeleteQuest(quest.questID);
			QuestDatabase.UpdateClients();
		} else if(action == 2) // Full edit
		{
			BetterQuesting.logger.log(Level.INFO, "Player " + sender.getCommandSenderName() + " made a database edit");
			JsonObject json1 = NBTConverter.NBTtoJSON_Compound(data.getCompoundTag("Data"), new JsonObject());
			JsonObject json2 = NBTConverter.NBTtoJSON_Compound(data.getCompoundTag("Progress"), new JsonObject());
			QuestDatabase.readFromJson(json1);
			QuestDatabase.readFromJson_Progression(json2);
			QuestDatabase.UpdateClients();
		} else if(action == 3) // Force Complete
		{
			if(quest == null || qID < 0)
			{
				BetterQuesting.logger.log(Level.ERROR, sender.getCommandSenderName() + " tried to force complete non-existent quest with ID:" + qID);
				return;
			}
			
			quest.setComplete(sender.getUniqueID(), 0);
			
			int done = 0;
			
			if(!quest.logic.GetResult(done, quest.tasks.size())) // Preliminary check
			{
				for(TaskBase task : quest.tasks)
				{
					task.setCompletion(sender.getUniqueID(), true);
					done += 1;
					
					if(quest.logic.GetResult(done, quest.tasks.size()))
					{
						break; // Only complete enough quests to claim the reward
					}
				}
			}
		} else if(action == 4) // Force Reset
		{
			if(quest == null || qID < 0)
			{
				BetterQuesting.logger.log(Level.ERROR, sender.getCommandSenderName() + " tried to force reset non-existent quest with ID:" + qID);
				return;
			}
			
			quest.ResetQuest();
		}
	}

	@Override
	public void handleClient(NBTTagCompound data)
	{
		return;
	}
}
