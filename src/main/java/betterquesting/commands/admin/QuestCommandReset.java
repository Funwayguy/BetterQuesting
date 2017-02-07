package betterquesting.commands.admin;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentTranslation;
import betterquesting.api.questing.IQuest;
import betterquesting.commands.QuestCommandBase;
import betterquesting.network.PacketSender;
import betterquesting.questing.QuestDatabase;
import betterquesting.storage.NameCache;

public class QuestCommandReset extends QuestCommandBase
{
	@Override
	public String getUsageSuffix()
	{
		return "[all|<quest_id>] [username|uuid]";
	}
	
	@Override
	public boolean validArgs(String[] args)
	{
		return args.length == 2 || args.length == 3;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public List<String> autoComplete(ICommandSender sender, String[] args)
	{
		ArrayList<String> list = new ArrayList<String>();
		
		if(args.length == 2)
		{
			list.add("all");
			
			for(int i : QuestDatabase.INSTANCE.getAllKeys())
			{
				list.add("" + i);
			}
		} else if(args.length == 3)
		{
			return CommandBase.getListOfStringsMatchingLastWord(args, NameCache.INSTANCE.getAllNames().toArray(new String[0]));
		}
		
		return list;
	}
	
	@Override
	public String getCommand()
	{
		return "reset";
	}
	
	@Override
	public void runCommand(CommandBase command, ICommandSender sender, String[] args)
	{
		String action = args[1];
		
		UUID uuid = null;
		
		if(args.length == 3)
		{
			uuid = this.findPlayerID(MinecraftServer.getServer(), args[2]);
			
			if(uuid == null)
			{
				throw this.getException(command);
			}
		}
		
		String pName = uuid == null? "NULL" : NameCache.INSTANCE.getName(uuid);
		
		if(action.equalsIgnoreCase("all"))
		{
			for(IQuest quest : QuestDatabase.INSTANCE.getAllValues())
			{
				if(uuid != null)
				{
					quest.resetUser(uuid, true); // Clear progress and state
				} else
				{
					quest.resetAll(true);
				}
			}
			
			if(uuid != null)
			{
				sender.addChatMessage(new ChatComponentTranslation("betterquesting.cmd.reset.player_all", pName));
			} else
			{
				sender.addChatMessage(new ChatComponentTranslation("betterquesting.cmd.reset.all_all"));
			}
		} else
		{
			try
			{
				int id = Integer.parseInt(action.trim());
				IQuest quest = QuestDatabase.INSTANCE.getValue(id);
				
				if(uuid != null)
				{
					quest.resetUser(uuid, true); // Clear progress and state
					sender.addChatMessage(new ChatComponentTranslation("betterquesting.cmd.reset.player_single", new ChatComponentTranslation(quest.getUnlocalisedName()), pName));
				} else
				{
					quest.resetAll(true);
					sender.addChatMessage(new ChatComponentTranslation("betterquesting.cmd.reset.all_single", new ChatComponentTranslation(quest.getUnlocalisedName())));
				}
			} catch(Exception e)
			{
				throw getException(command);
			}
		}
		
		PacketSender.INSTANCE.sendToAll(QuestDatabase.INSTANCE.getSyncPacket());
	}
	
	@Override
	public boolean isArgUsername(String[] args, int index)
	{
		return index == 2;
	}
}
