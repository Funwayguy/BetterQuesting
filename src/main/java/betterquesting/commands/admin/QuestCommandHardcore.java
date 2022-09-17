package betterquesting.commands.admin;

import betterquesting.api.properties.NativeProps;
import betterquesting.commands.QuestCommandBase;
import betterquesting.handlers.SaveLoadHandler;
import betterquesting.network.handlers.NetSettingSync;
import betterquesting.storage.QuestSettings;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentTranslation;

import java.util.ArrayList;
import java.util.List;

public class QuestCommandHardcore extends QuestCommandBase
{
	@Override
	public String getCommand()
	{
		return "hardcore";
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
		boolean flag = !QuestSettings.INSTANCE.getProperty(NativeProps.HARDCORE);
		
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
		
		QuestSettings.INSTANCE.setProperty(NativeProps.HARDCORE, flag);
        SaveLoadHandler.INSTANCE.markDirty();
        
		sender.addChatMessage(new ChatComponentTranslation("betterquesting.cmd.hardcore", new ChatComponentTranslation(QuestSettings.INSTANCE.getProperty(NativeProps.HARDCORE) ? "options.on" : "options.off")));
        NetSettingSync.sendSync(null);
	}
}
