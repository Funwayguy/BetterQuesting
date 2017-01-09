package betterquesting.commands.admin;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentTranslation;
import betterquesting.api.properties.NativeProps;
import betterquesting.api.questing.IQuest;
import betterquesting.api.questing.tasks.ITask;
import betterquesting.commands.QuestCommandBase;
import betterquesting.network.PacketSender;
import betterquesting.questing.QuestDatabase;
import betterquesting.storage.NameCache;

public class QuestCommandComplete extends QuestCommandBase
{
	public String getUsageSuffix()
	{
		return "<quest_id> [username|uuid]";
	}
	
	public boolean validArgs(String[] args)
	{
		return args.length == 2 || args.length == 3;
	}
	
	public List<String> autoComplete(MinecraftServer server, ICommandSender sender, String[] args)
	{
		ArrayList<String> list = new ArrayList<String>();
		
		if(args.length == 2)
		{
			for(int i : QuestDatabase.INSTANCE.getAllKeys())
			{
				list.add("" + i);
			}
		} else if(args.length == 3)
		{
			return CommandBase.getListOfStringsMatchingLastWord(args, NameCache.INSTANCE.getAllNames());
		}
		
		return list;
	}
	
	@Override
	public String getCommand()
	{
		return "complete";
	}
	
	@Override
	public void runCommand(MinecraftServer server, CommandBase command, ICommandSender sender, String[] args) throws CommandException
	{
		UUID uuid = null;
		
		if(args.length >= 3)
		{
			uuid = this.findPlayerID(server, args[2]);
			
			if(uuid == null)
			{
				throw this.getException(command);
			}
		}
		
		String pName = uuid == null? "NULL" : NameCache.INSTANCE.getName(uuid);
		
		try
		{
			int id = Integer.parseInt(args[1].trim());
			IQuest quest = QuestDatabase.INSTANCE.getValue(id);
			quest.setComplete(uuid, 0);
			
			int done = 0;
			
			if(!quest.getProperties().getProperty(NativeProps.LOGIC_TASK).getResult(done, quest.getTasks().size())) // Preliminary check
			{
				for(ITask task : quest.getTasks().getAllValues())
				{
					task.setComplete(uuid);
					done += 1;
					
					if(quest.getProperties().getProperty(NativeProps.LOGIC_TASK).getResult(done, quest.getTasks().size()))
					{
						break; // Only complete enough quests to claim the reward
					}
				}
			}
			
			sender.addChatMessage(new TextComponentTranslation("betterquesting.cmd.complete", new TextComponentTranslation(quest.getUnlocalisedName()), pName));
		} catch(Exception e)
		{
			throw getException(command);
		}
		
		PacketSender.INSTANCE.sendToAll(QuestDatabase.INSTANCE.getSyncPacket());
	}
}
