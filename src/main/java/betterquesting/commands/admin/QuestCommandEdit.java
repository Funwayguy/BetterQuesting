package betterquesting.commands.admin;

import betterquesting.api.properties.NativeProps;
import betterquesting.commands.QuestCommandBase;
import betterquesting.handlers.SaveLoadHandler;
import betterquesting.network.PacketSender;
import betterquesting.storage.NameCache;
import betterquesting.storage.QuestSettings;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentTranslation;

import java.util.ArrayList;
import java.util.List;

public class QuestCommandEdit extends QuestCommandBase
{
	@Override
	public String getCommand()
	{
		return "edit";
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
	public List<String> autoComplete(MinecraftServer server, ICommandSender sender, String[] args)
	{
		ArrayList<String> list = new ArrayList<String>();
		
		if(args.length == 2)
		{
			return CommandBase.getListOfStringsMatchingLastWord(args, new String[]{"true","false"});
		}
		
		return list;
	}
	
	@Override
	public void runCommand(MinecraftServer server, CommandBase command, ICommandSender sender, String[] args) throws CommandException
	{
		boolean flag = !QuestSettings.INSTANCE.getProperty(NativeProps.EDIT_MODE);
		
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
		
		if(flag)
		{
			NameCache.INSTANCE.updateNames(server);
		}
		
        SaveLoadHandler.INSTANCE.markDirty();
		
		QuestSettings.INSTANCE.setProperty(NativeProps.EDIT_MODE, flag);
		sender.sendMessage(new TextComponentTranslation("betterquesting.cmd.edit", new TextComponentTranslation(QuestSettings.INSTANCE.getProperty(NativeProps.EDIT_MODE)? "options.on" : "options.off")));
		PacketSender.INSTANCE.sendToAll(QuestSettings.INSTANCE.getSyncPacket());
	}
}
