package betterquesting.commands.admin;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentTranslation;
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
	
	@SuppressWarnings("unchecked")
	public List<String> autoComplete(ICommandSender sender, String[] args)
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
			return CommandBase.getListOfStringsMatchingLastWord(args, MinecraftServer.getServer().getAllUsernames());
		}
		
		return list;
	}
	
	@Override
	public String getCommand()
	{
		return "reset";
	}
	
	@Override
	public void runCommand(CommandBase command, ICommandSender sender, String[] args)
	{
		String action = args[1];
		
		UUID uuid = null;
		EntityPlayerMP player = null;
		
		if(args.length == 3)
		{
			player = MinecraftServer.getServer().getConfigurationManager().func_152612_a(args[2]);
			
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
		
		String pName = player != null? player.getCommandSenderName() : (uuid != null? uuid.toString() : null);
		
		if(action.equalsIgnoreCase("all"))
		{
			for(QuestInstance quest : QuestDatabase.questDB.values())
			{
				if(uuid != null)
				{
					quest.ResetQuest(uuid); // Clear progress and state
				} else
				{
					quest.ResetQuest();
				}
			}
			
			if(uuid != null)
			{
				sender.addChatMessage(new ChatComponentTranslation("betterquesting.cmd.reset.player_all", pName));
			} else
			{
				sender.addChatMessage(new ChatComponentTranslation("betterquesting.cmd.reset.all_all"));
			}
		} else
		{
			try
			{
				int id = Integer.parseInt(action.trim());
				QuestInstance quest = QuestDatabase.getQuestByID(id);
				
				if(uuid != null)
				{
					quest.ResetQuest(uuid); // Clear progress and state
					sender.addChatMessage(new ChatComponentTranslation("betterquesting.cmd.reset.player_single", new ChatComponentTranslation(quest.name), pName));
				} else
				{
					quest.ResetQuest();
					sender.addChatMessage(new ChatComponentTranslation("betterquesting.cmd.reset.all_single", new ChatComponentTranslation(quest.name)));
				}
			} catch(Exception e)
			{
				throw getException(command);
			}
		}
		
		QuestDatabase.UpdateClients();
	}	
}
