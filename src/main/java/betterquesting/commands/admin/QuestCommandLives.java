package betterquesting.commands.admin;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import betterquesting.commands.QuestCommandBase;
import betterquesting.lives.LifeManager;

public class QuestCommandLives extends QuestCommandBase
{
	@Override
	public String getCommand()
	{
		return "lives";
	}
	
	public String getUsageSuffix()
	{
		return "[add|set|max|default] <value> [username]";
	}
	
	public boolean validArgs(String[] args)
	{
		return args.length == 4 || args.length == 3;
	}
	
	@Override
	public List<String> autoComplete(MinecraftServer server, ICommandSender sender, String[] args)
	{
		ArrayList<String> list = new ArrayList<String>();
		
		if(args.length == 4 && (args[1].equalsIgnoreCase("add") || args[1].equalsIgnoreCase("set")))
		{
			return CommandBase.getListOfStringsMatchingLastWord(args, server.getAllUsernames());
		} else if(args.length == 2)
		{
			return CommandBase.getListOfStringsMatchingLastWord(args, new String[]{"add","set","max","default"});
		}
		
		return list;
	}
	
	@Override
	public void runCommand(MinecraftServer server, CommandBase command, ICommandSender sender, String[] args) throws CommandException
	{
		String action = args[1];
		EntityPlayerMP player = args.length < 4? null : server.getPlayerList().getPlayerByUsername(args[3]);
		
		int value = 0;
		
		try
		{
			value = Integer.parseInt(args[2]);
		} catch(Exception e)
		{
			throw getException(command);
		}
		
		if(action.equalsIgnoreCase("set"))
		{
			if(player != null)
			{
				LifeManager.setLives(player, value);
				sender.addChatMessage(new TextComponentString("Set " + player.getName() + " lives to " + value));
			} else if(args.length == 3)
			{
				for(EntityPlayerMP p : server.getPlayerList().getPlayerList())
				{
					LifeManager.setLives(p, value);
				}
				
				sender.addChatMessage(new TextComponentString("Set all player's lives to " + value));
			}
		} else if(action.equalsIgnoreCase("add"))
		{
			if(player != null)
			{
				LifeManager.AddRemoveLives(player, value);
				sender.addChatMessage(new TextComponentString((value >= 0? "Added " : "Removed ") + Math.abs(value) + " lives " + (value >= 0? "to " : "from ") + player.getName() + " (Total: " + LifeManager.getLives(player) + ")"));
			} else
			{
				for(EntityPlayerMP p : server.getPlayerList().getPlayerList())
				{
					LifeManager.AddRemoveLives(p, value);
				}
				sender.addChatMessage(new TextComponentString((value >= 0? "Added " : "Removed ") + Math.abs(value) + " lives " + (value >= 0? "to " : "from ") + " all players"));
			}
		} else if(action.equalsIgnoreCase("max"))
		{
			value = Math.max(1, value);
			LifeManager.maxLives = value;
			sender.addChatMessage(new TextComponentString("Set max lives to " + value));
		} else if(action.equalsIgnoreCase("default"))
		{
			value = Math.max(1, value);
			LifeManager.maxLives = value;
			sender.addChatMessage(new TextComponentString("Set default lives to " + value));
		} else
		{
			throw getException(command);
		}
	}
}
