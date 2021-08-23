package betterquesting.commands.user;

import betterquesting.api.storage.BQ_Settings;
import betterquesting.commands.QuestCommandBase;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentTranslation;

import java.util.ArrayList;
import java.util.List;

public class QuestCommandView extends QuestCommandBase
{
	@Override
	public String getCommand()
	{
		return "view";
	}
	
	@Override
	public String getUsageSuffix()
	{
		return "[true|false]";
	}
	
	@Override
	public boolean validArgs(String[] args)
	{
		return args.length == 1 || args.length == 2;
	}
	
	@Override
    @SuppressWarnings("unchecked")
	public List<String> autoComplete(MinecraftServer server, ICommandSender sender, String[] args)
	{
		List<String> list = new ArrayList<>();
		
		if(args.length == 2)
		{
			return CommandBase.getListOfStringsMatchingLastWord(args, "true","false");
		}
		
		return list;
	}
	
	@Override
	public void runCommand(MinecraftServer server, CommandBase command, ICommandSender sender, String[] args)
	{
		boolean flag = !BQ_Settings.viewModeBtn;
		
		if(args.length == 2)
		{
			try
			{
				if(args[1].equalsIgnoreCase("on"))
				{
					flag = true;
				} else if(args[1].equalsIgnoreCase("off"))
				{
					flag = false;
				} else
				{
					flag = Boolean.parseBoolean(args[1]);
				}
			} catch(Exception e)
			{
				throw this.getException(command);
			}
		}

		BQ_Settings.viewModeBtn = flag;

		sender.addChatMessage(new ChatComponentTranslation("betterquesting.cmd.view", new ChatComponentTranslation(BQ_Settings.viewModeBtn ? "options.on" : "options.off")));
	}
}
