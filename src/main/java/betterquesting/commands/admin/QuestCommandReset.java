package betterquesting.commands.admin;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.translation.I18n;
import betterquesting.commands.QuestCommandBase;
import betterquesting.quests.QuestDatabase;
import betterquesting.quests.QuestInstance;

public class QuestCommandReset extends QuestCommandBase
{
	public String getUsageSuffix()
	{
		return "[all|<quest_id>] [username|uuid]";
	}
	
	public boolean validArgs(String[] args)
	{
		return args.length == 2 || args.length == 3;
	}
	
	public List<String> autoComplete(MinecraftServer server, ICommandSender sender, String[] args)
	{
		ArrayList<String> list = new ArrayList<String>();
		
		if(args.length == 2)
		{
			list.add("all");
			
			for(int i : QuestDatabase.questDB.keySet())
			{
				list.add("" + i);
			}
		} else if(args.length == 3)
		{
			return CommandBase.getListOfStringsMatchingLastWord(args, server.getPlayerList().getAllUsernames());
		}
		
		return list;
	}
	
	@Override
	public String getCommand()
	{
		return "reset";
	}
	
	@Override
	public void runCommand(MinecraftServer server, CommandBase command, ICommandSender sender, String[] args) throws CommandException
	{
		String action = args[1];
		
		UUID uuid = null;
		EntityPlayerMP player = null;
		
		if(args.length == 3)
		{
			player = server.getPlayerList().getPlayerByUsername(args[2]);
			
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
		}
		
		if(action.equalsIgnoreCase("all"))
		{
			for(QuestInstance quest : new ArrayList<QuestInstance>(QuestDatabase.questDB.values()))
			{
				if(uuid != null)
				{
					quest.ResetProgress(uuid); // Clear progress
					quest.RemoveUserEntry(uuid); // Clear completion state
				} else
				{
					quest.ResetQuest();
				}
			}
			
			sender.addChatMessage(new TextComponentString("Reset all quests" + (player != null? " for " + player.getName() : (uuid != null? " for " + uuid.toString() : ""))));
		} else
		{
			try
			{
				int id = Integer.parseInt(action.trim());
				QuestInstance quest = QuestDatabase.getQuestByID(id);
				
				if(uuid != null)
				{
					quest.ResetProgress(uuid); // Clear progress
					quest.RemoveUserEntry(uuid); // Clear completion state
				} else
				{
					quest.ResetQuest();
				}
				
				sender.addChatMessage(new TextComponentString("Reset quest " + I18n.translateToLocal(quest.name) +"(ID:" + id + ")" + (player != null? " for " + player.getName() : (uuid != null? " for " + uuid.toString() : ""))));
			} catch(Exception e)
			{
				throw getException(command);
			}
		}
		
		QuestDatabase.UpdateClients();
	}	
}
