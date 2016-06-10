package betterquesting.commands.admin;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentTranslation;
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
	
	@SuppressWarnings("unchecked")
	public List<String> autoComplete(ICommandSender sender, String[] args)
	{
		ArrayList<String> list = new ArrayList<String>();
		
		if(args.length == 4 && (args[1].equalsIgnoreCase("add") || args[1].equalsIgnoreCase("set")))
		{
			return CommandBase.getListOfStringsMatchingLastWord(args, MinecraftServer.getServer().getAllUsernames());
		} else if(args.length == 2)
		{
			return CommandBase.getListOfStringsMatchingLastWord(args, new String[]{"add","set","max","default"});
		}
		
		return list;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public void runCommand(CommandBase command, ICommandSender sender, String[] args)
	{
		String action = args[1];
		EntityPlayerMP player = args.length < 4? null : MinecraftServer.getServer().getConfigurationManager().func_152612_a(args[3]);
		
		if(player == null && args.length == 4)
		{
			throw getException(command);
		}
		
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
			value = Math.max(1, value);
			if(player != null)
			{
				LifeManager.setLives(player, value);
				sender.addChatMessage(new ChatComponentTranslation("betterquesting.cmd.lives.set_player", player.getCommandSenderName(), value));
			} else if(args.length == 3)
			{
				for(EntityPlayer p : (List<EntityPlayer>)MinecraftServer.getServer().getConfigurationManager().playerEntityList)
				{
					LifeManager.setLives(p, value);
				}
				
				sender.addChatMessage(new ChatComponentTranslation("betterquesting.cmd.lives.set_all", value));
			}
		} else if(action.equalsIgnoreCase("add"))
		{
			if(player != null)
			{
				LifeManager.AddRemoveLives(player, value);
				
				if(value >= 0)
				{
					sender.addChatMessage(new ChatComponentTranslation("betterquesting.cmd.lives.add_player", value, player.getCommandSenderName(), LifeManager.getLives(player)));
				} else
				{
					sender.addChatMessage(new ChatComponentTranslation("betterquesting.cmd.lives.remove_player", Math.abs(value), player.getCommandSenderName(), LifeManager.getLives(player)));
				}
			} else
			{
				for(EntityPlayer p : (List<EntityPlayer>)MinecraftServer.getServer().getConfigurationManager().playerEntityList)
				{
					LifeManager.AddRemoveLives(p, value);
				}
				
				if(value >= 0)
				{
					sender.addChatMessage(new ChatComponentTranslation("betterquesting.cmd.lives.add_all", value));
				} else
				{
					sender.addChatMessage(new ChatComponentTranslation("betterquesting.cmd.lives.remove_all", Math.abs(value)));
				}
			}
		} else if(action.equalsIgnoreCase("max"))
		{
			value = Math.max(1, value);
			LifeManager.maxLives = value;
			sender.addChatMessage(new ChatComponentTranslation("betterquesting.cmd.lives.max", value));
		} else if(action.equalsIgnoreCase("default"))
		{
			value = Math.max(1, value);
			LifeManager.defLives = value;
			sender.addChatMessage(new ChatComponentTranslation("betterquesting.cmd.lives.default" + value));
		} else
		{
			throw getException(command);
		}
	}
}
