package betterquesting.commands.admin;

import betterquesting.api.properties.NativeProps;
import betterquesting.api.questing.IQuest;
import betterquesting.api.questing.tasks.ITask;
import betterquesting.api2.storage.DBEntry;
import betterquesting.commands.QuestCommandBase;
import betterquesting.network.PacketSender;
import betterquesting.questing.QuestDatabase;
import betterquesting.storage.NameCache;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.server.permission.DefaultPermissionLevel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class QuestCommandComplete extends QuestCommandBase
{
	@Override
	public String getUsageSuffix()
	{
		return "<quest_id> [username|uuid]";
	}
	
	@Override
	public boolean validArgs(String[] args)
	{
		return args.length == 2 || args.length == 3;
	}
	
	@Override
	public List<String> autoComplete(MinecraftServer server, ICommandSender sender, String[] args)
	{
		if(args.length == 2)
		{
		    List<String> list = new ArrayList<>();
			for(DBEntry<IQuest> i : QuestDatabase.INSTANCE.getEntries())
			{
				list.add("" + i.getID());
			}
			return list;
		} else if(args.length == 3)
		{
			return CommandBase.getListOfStringsMatchingLastWord(args, NameCache.INSTANCE.getAllNames());
		}
		
		return Collections.emptyList();
	}
	
	@Override
	public String getCommand()
	{
		return "complete";
	}
	
	@Override
	public void runCommand(MinecraftServer server, CommandBase command, ICommandSender sender, String[] args) throws CommandException
	{
		UUID uuid;
		
		if(args.length >= 3)
		{
			uuid = this.findPlayerID(server, sender, args[2]);
			
			if(uuid == null)
			{
				throw this.getException(command);
			}
		} else
		{
			uuid = this.findPlayerID(server, sender, sender.getName());
		}
		
		String pName = uuid == null? "NULL" : NameCache.INSTANCE.getName(uuid);
		
		try
		{
			int id = Integer.parseInt(args[1].trim());
			IQuest quest = QuestDatabase.INSTANCE.getValue(id);
			quest.setComplete(uuid, 0);
			
			int done = 0;
			
			if(!quest.getProperty(NativeProps.LOGIC_TASK).getResult(done, quest.getTasks().size())) // Preliminary check
			{
				for(DBEntry<ITask> task : quest.getTasks().getEntries())
				{
					task.getValue().setComplete(uuid);
					done += 1;
					
					if(quest.getProperty(NativeProps.LOGIC_TASK).getResult(done, quest.getTasks().size()))
					{
						break; // Only complete enough quests to claim the reward
					}
				}
			}
			
			sender.sendMessage(new TextComponentTranslation("betterquesting.cmd.complete", new TextComponentTranslation(quest.getProperty(NativeProps.NAME)), pName));
		} catch(Exception e)
		{
			throw getException(command);
		}
		
		PacketSender.INSTANCE.sendToAll(QuestDatabase.INSTANCE.getSyncPacket());
	}
	
	@Override
	public boolean isArgUsername(String[] args, int index)
	{
		return index == 2;
	}

	@Override
	public String getPermissionNode() 
	{
		return "betterquesting.command.admin.complete";
	}

	@Override
	public DefaultPermissionLevel getPermissionLevel() 
	{
		return DefaultPermissionLevel.OP;
	}

	@Override
	public String getPermissionDescription() 
	{
		return "Permission to force completes a quest for the given user";
	}
}
