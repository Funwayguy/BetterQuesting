package betterquesting.commands.admin;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
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
	
	@SuppressWarnings("unchecked")
	public List<String> autoComplete(ICommandSender sender, String[] args)
	{
		ArrayList<String> list = new ArrayList<String>();
		
		if(args.length == 2)
		{
			return CommandBase.getListOfStringsMatchingLastWord(args, MinecraftServer.getServer().getAllUsernames());
		} else if(args.length == 3)
		{
			list.add("add");
			list.add("set");
		}
		
		return list;
	}
	
	@Override
	public void runCommand(CommandBase command, ICommandSender sender, String[] args)
	{
		String action = args[2];
		EntityPlayerMP player = MinecraftServer.getServer().getConfigurationManager().func_152612_a(args[1]);
		
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
			sender.addChatMessage(new ChatComponentText("Set " + player.getCommandSenderName() + " lives to " + value));
		} else if(action.equalsIgnoreCase("add"))
		{
			LifeManager.AddRemoveLives(player, value);
			sender.addChatMessage(new ChatComponentText((value >= 0? "Added " : "Removed ") + value + " lives from " + player.getCommandSenderName() + " (Total: " + LifeManager.getLives(player) + ")"));
		} else
		{
			throw getException(command);
		}
	}
}
