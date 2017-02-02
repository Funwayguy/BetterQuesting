package betterquesting.commands.admin;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentTranslation;
import betterquesting.api.properties.NativeProps;
import betterquesting.commands.QuestCommandBase;
import betterquesting.network.PacketSender;
import betterquesting.storage.QuestSettings;

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
	public List<String> autoComplete(ICommandSender sender, String[] args)
	{
		ArrayList<String> list = new ArrayList<String>();
		
		if(args.length == 2)
		{
			return CommandBase.getListOfStringsMatchingLastWord(args, new String[]{"true","false"});
		}
		
		return list;
	}
	
	@Override
	public void runCommand(CommandBase command, ICommandSender sender, String[] args)
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
		sender.addChatMessage(new ChatComponentTranslation("betterquesting.cmd.hardcore", new ChatComponentTranslation(QuestSettings.INSTANCE.getProperty(NativeProps.HARDCORE)? "options.on" : "options.off")));
		PacketSender.INSTANCE.sendToAll(QuestSettings.INSTANCE.getSyncPacket());
	}
}
