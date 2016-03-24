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
		return "<username> [add/set] <value>";
	}
	
	public boolean validArgs(String[] args)
	{
		return args.length == 4;
	}
	
	@Override
	public List<String> autoComplete(MinecraftServer server, ICommandSender sender, String[] args)
	{
		ArrayList<String> list = new ArrayList<String>();
		
		if(args.length == 2)
		{
			return CommandBase.getListOfStringsMatchingLastWord(args, server.getAllUsernames());
		} else if(args.length == 3)
		{
			list.add("add");
			list.add("set");
		}
		
		return list;
	}
	
	@Override
	public void runCommand(MinecraftServer server, CommandBase command, ICommandSender sender, String[] args) throws CommandException
	{
		String action = args[2];
		EntityPlayerMP player = server.getPlayerList().getPlayerByUsername(args[1]);
		
		if(player == null)
		{
			throw getException(command);
		}
		
		int value = 0;
		
		try
		{
			value = Integer.parseInt(args[3]);
		} catch(Exception e)
		{
			throw getException(command);
		}
		
		if(action.equalsIgnoreCase("set"))
		{
			LifeManager.setLives(player, value);
			sender.addChatMessage(new TextComponentString("Set " + player.getName() + " lives to " + value));
		} else if(action.equalsIgnoreCase("add"))
		{
			LifeManager.AddRemoveLives(player, value);
			sender.addChatMessage(new TextComponentString((value >= 0? "Added " : "Removed ") + value + " lives from " + player.getName() + " (Total: " + LifeManager.getLives(player) + ")"));
		} else
		{
			throw getException(command);
		}
	}
}
