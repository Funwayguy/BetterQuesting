package betterquesting.commands.admin;

import betterquesting.api.properties.NativeProps;
import betterquesting.api.questing.IQuest;
import betterquesting.api2.storage.DBEntry;
import betterquesting.commands.QuestCommandBase;
import betterquesting.handlers.SaveLoadHandler;
import betterquesting.network.PacketSender;
import betterquesting.questing.QuestDatabase;
import betterquesting.questing.QuestLineDatabase;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.server.permission.DefaultPermissionLevel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class QuestCommandDelete extends QuestCommandBase
{
	@Override
	public String getUsageSuffix()
	{
		return "[all|<quest_id>]";
	}
	
	@Override
	public boolean validArgs(String[] args)
	{
		return args.length == 2;
	}
	
	@Override
	public List<String> autoComplete(MinecraftServer server, ICommandSender sender, String[] args)
	{
		if(args.length == 2)
		{
		    List<String> list = new ArrayList<>();
			list.add("all");
			
			for(DBEntry<IQuest> i : QuestDatabase.INSTANCE.getEntries())
			{
				list.add("" + i.getID());
			}
			return list;
		}
		
		return Collections.emptyList();
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
			QuestDatabase.INSTANCE.reset();
			QuestLineDatabase.INSTANCE.reset();
			PacketSender.INSTANCE.sendToAll(QuestDatabase.INSTANCE.getSyncPacket());
			PacketSender.INSTANCE.sendToAll(QuestLineDatabase.INSTANCE.getSyncPacket());
		    
			sender.sendMessage(new TextComponentTranslation("betterquesting.cmd.delete.all"));
            SaveLoadHandler.INSTANCE.markDirty();
		} else
		{
			try
			{
				int id = Integer.parseInt(args[1].trim());
				IQuest quest = QuestDatabase.INSTANCE.getValue(id);
				QuestDatabase.INSTANCE.removeID(id);
				PacketSender.INSTANCE.sendToAll(QuestDatabase.INSTANCE.getSyncPacket());
				
				sender.sendMessage(new TextComponentTranslation("betterquesting.cmd.delete.single", new TextComponentTranslation(quest.getProperty(NativeProps.NAME))));
                SaveLoadHandler.INSTANCE.markDirty();
			} catch(Exception e)
			{
				throw getException(command);
			}
		}
	}
	
	@Override
	public String getPermissionNode() 
	{
		return "betterquesting.command.admin.delete";
	}

	@Override
	public DefaultPermissionLevel getPermissionLevel() 
	{
		return DefaultPermissionLevel.OP;
	}

	@Override
	public String getPermissionDescription() 
	{
		return "Permission to delete given quest(s) and progression data however it does not delete new world defaults";
	}
}
