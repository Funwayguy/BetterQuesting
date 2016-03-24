package betterquesting.commands.admin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.translation.I18n;
import betterquesting.commands.QuestCommandBase;
import betterquesting.quests.QuestDatabase;
import betterquesting.quests.QuestInstance;
import betterquesting.quests.QuestLine;

public class QuestCommandDelete extends QuestCommandBase
{
	public String getUsageSuffix()
	{
		return "[all|<quest_id>]";
	}
	
	public boolean validArgs(String[] args)
	{
		return args.length == 2;
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
		}
		
		return list;
	}
	
	@Override
	public String getCommand()
	{
		return "delete";
	}
	
	@Override
	public void runCommand(MinecraftServer server, CommandBase command, ICommandSender sender, String[] args) throws CommandException
	{
		if(args[1].equalsIgnoreCase("all"))
		{
			QuestDatabase.questDB = new HashMap<Integer,QuestInstance>();
			QuestDatabase.questLines = new ArrayList<QuestLine>();
			QuestDatabase.UpdateClients();
		    
			sender.addChatMessage(new TextComponentString("Deleted all quests and quest lines"));
		} else
		{
			try
			{
				int id = Integer.parseInt(args[1].trim());
				QuestInstance quest = QuestDatabase.getQuestByID(id);
				QuestDatabase.DeleteQuest(id);
				
				sender.addChatMessage(new TextComponentString("Deleted quest " + I18n.translateToLocal(quest.name) +"(ID:" + id + ")"));
			} catch(Exception e)
			{
				throw getException(command);
			}
		}
		
		QuestDatabase.UpdateClients();
	}
	
}
