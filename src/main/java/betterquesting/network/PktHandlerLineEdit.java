package betterquesting.network;

import org.apache.logging.log4j.Level;
import betterquesting.core.BetterQuesting;
import betterquesting.quests.QuestDatabase;
import betterquesting.quests.QuestInstance;
import betterquesting.quests.QuestLine;
import betterquesting.utils.NBTConverter;
import com.google.gson.JsonObject;
import com.mojang.realmsclient.gui.ChatFormatting;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import cpw.mods.fml.common.network.simpleimpl.IMessage;

public class PktHandlerLineEdit extends PktHandler
{
	@Override
	public IMessage handleServer(EntityPlayer sender, NBTTagCompound data)
	{
		if(sender == null)
		{
			return null;
		}
		
		if(!MinecraftServer.getServer().getConfigurationManager().func_152596_g(sender.getGameProfile()))
		{
			BetterQuesting.logger.log(Level.WARN, "Player " + sender.getCommandSenderName() + " (UUID:" + sender.getUniqueID() + ") tried to edit quest lines without OP permissions!");
			sender.addChatComponentMessage(new ChatComponentText(ChatFormatting.RED + "You need to be OP to edit quests!"));
			return null; // Player is not operator. Do nothing
		}
		
		int action = !data.hasKey("action")? -1 : data.getInteger("action");
		
		if(action < 0)
		{
			BetterQuesting.logger.log(Level.ERROR, sender.getCommandSenderName() + " tried to perform invalid quest edit action: " + action);
			return null;
		}
		
		if(action == 0) // Add new QuestLine
		{
			QuestDatabase.questLines.add(new QuestLine());
		} else if(action == 1) // Add new QuestInstance
		{
			new QuestInstance(QuestDatabase.getUniqueID(), true);
		} else if(action == 2) // Edit quest lines
		{
			QuestDatabase.readFromJson_Lines(NBTConverter.NBTtoJSON_Compound(data.getCompoundTag("Data"), new JsonObject()));
		}
		
		QuestDatabase.UpdateClients(); // Update all clients with new quest data
		return null;
	}
	
	@Override
	public IMessage handleClient(NBTTagCompound data)
	{
		return null;
	}
}
