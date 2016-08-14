package betterquesting.network.handlers;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;
import org.apache.logging.log4j.Level;
import betterquesting.api.enums.EnumSaveType;
import betterquesting.api.network.IPacketHandler;
import betterquesting.api.network.PacketTypeNative;
import betterquesting.api.quests.IQuestContainer;
import betterquesting.api.quests.properties.QuestProperties;
import betterquesting.api.quests.tasks.ITaskBase;
import betterquesting.api.utils.JsonHelper;
import betterquesting.api.utils.NBTConverter;
import betterquesting.core.BetterQuesting;
import betterquesting.quests.QuestDatabase;
import com.google.gson.JsonObject;

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
		
		if(!MinecraftServer.getServer().getConfigurationManager().func_152596_g(sender.getGameProfile()))
		{
			BetterQuesting.logger.log(Level.WARN, "Player " + sender.getCommandSenderName() + " (UUID:" + sender.getUniqueID() + ") tried to edit quests without OP permissions!");
			sender.addChatComponentMessage(new ChatComponentText(EnumChatFormatting.RED + "You need to be OP to edit quests!"));
			return; // Player is not operator. Do nothing
		}
		
		int action = !data.hasKey("action")? -1 : data.getInteger("action");
		int qID = !data.hasKey("questID")? -1 : data.getInteger("questID");
		IQuestContainer quest = QuestDatabase.INSTANCE.getValue(qID);
		//JsonObject base = NBTConverter.NBTtoJSON_Compound(data.getCompoundTag("data"), new JsonObject());
		
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
			
			int ps = quest.getPrerequisites().size();
			
			BetterQuesting.logger.log(Level.INFO, "Player " + sender.getCommandSenderName() + " edited quest " + quest.getUnlocalisedName());
			JsonObject json1 = NBTConverter.NBTtoJSON_Compound(data.getCompoundTag("Data"), new JsonObject());
			quest.readFromJson(json1, EnumSaveType.CONFIG);
			JsonObject json2 = NBTConverter.NBTtoJSON_Compound(data.getCompoundTag("Progress"), new JsonObject());
			quest.readFromJson(json2, EnumSaveType.PROGRESS);
			
			if(ps != quest.getPrerequisites().size())
			{
				QuestDatabase.INSTANCE.syncAll();
			} else
			{
				quest.syncAll();
			}
		} else if(action == 1) // Delete quest
		{
			if(quest == null || qID < 0)
			{
				BetterQuesting.logger.log(Level.ERROR, sender.getCommandSenderName() + " tried to delete non-existent quest with ID:" + qID);
				return;
			}
			
			BetterQuesting.logger.log(Level.INFO, "Player " + sender.getCommandSenderName() + " deleted quest " + quest.getUnlocalisedName());
			QuestDatabase.INSTANCE.remove(qID);
			QuestDatabase.INSTANCE.syncAll();
		} else if(action == 2) // Full edit
		{
			BetterQuesting.logger.log(Level.INFO, "Player " + sender.getCommandSenderName() + " made a database edit");
			JsonObject base = NBTConverter.NBTtoJSON_Compound(data.getCompoundTag("data"), new JsonObject());
			QuestDatabase.INSTANCE.readFromJson(JsonHelper.GetArray(base, "database"), EnumSaveType.CONFIG);
			QuestDatabase.INSTANCE.readFromJson(JsonHelper.GetArray(base, "progress"), EnumSaveType.PROGRESS);
			QuestDatabase.INSTANCE.syncAll();
		} else if(action == 3) // Force Complete
		{
			if(quest == null || qID < 0)
			{
				BetterQuesting.logger.log(Level.ERROR, sender.getCommandSenderName() + " tried to force complete non-existent quest with ID:" + qID);
				return;
			}
			
			quest.setComplete(sender.getUniqueID(), 0);
			
			int done = 0;
			
			if(!quest.getInfo().getProperty(QuestProperties.LOGIC_TASK).GetResult(done, quest.getTasks().size())) // Preliminary check
			{
				for(ITaskBase task : quest.getTasks().getAllValues())
				{
					task.setComplete(sender.getUniqueID());
					done += 1;
					
					if(quest.getInfo().getProperty(QuestProperties.LOGIC_TASK).GetResult(done, quest.getTasks().size()))
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
			
			quest.resetAll(true);
		}
	}

	@Override
	public void handleClient(NBTTagCompound data)
	{
		return;
	}
}
