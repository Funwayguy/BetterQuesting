package betterquesting.commands.admin;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.client.resources.I18n;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import betterquesting.commands.QuestCommandBase;
import betterquesting.quests.QuestDatabase;
import betterquesting.quests.QuestInstance;

public class QuestCommandComplete extends QuestCommandBase
{
	public String getUsageSuffix()
	{
		return "<quest_id> [username|uuid]";
	}
	
	public boolean validArgs(String[] args)
	{
		return args.length == 3;
	}
	
	public List<String> autoComplete(ICommandSender sender, String[] args)
	{
		ArrayList<String> list = new ArrayList<String>();
		
		if(args.length == 2)
		{
			for(int i : QuestDatabase.questDB.keySet())
			{
				list.add("" + i);
			}
		} else if(args.length == 3)
		{
			return CommandBase.getListOfStringsMatchingLastWord(args, MinecraftServer.getServer().getAllUsernames());
		}
		
		return list;
	}
	
	@Override
	public String getCommand()
	{
		return "complete";
	}
	
	@Override
	public void runCommand(CommandBase command, ICommandSender sender, String[] args) throws CommandException
	{
		UUID uuid = null;
		EntityPlayerMP player = null;
		
		player = MinecraftServer.getServer().getConfigurationManager().getPlayerByUsername(args[2]);
		
		if(player == null)
		{
			try
			{
				uuid = UUID.fromString(args[2]);
			} catch(Exception e)
			{
				throw getException(command);
			}
		} else
		{
			uuid = player.getUniqueID();
		}
		
		try
		{
			int id = Integer.parseInt(args[1].trim());
			QuestInstance quest = QuestDatabase.getQuestByID(id);
			quest.setComplete(uuid, 0);
			
			sender.addChatMessage(new ChatComponentText("Manually completed quest " + I18n.format(quest.name) +"(ID:" + id + ")" + (player != null? " for " + player.getName() : (uuid != null? " for " + uuid.toString() : ""))));
		} catch(Exception e)
		{
			throw getException(command);
		}
		
		QuestDatabase.UpdateClients();
	}
}
