package adv_director.commands.admin;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentTranslation;
import adv_director.api.questing.IQuest;
import adv_director.commands.QuestCommandBase;
import adv_director.network.PacketSender;
import adv_director.questing.QuestDatabase;
import adv_director.storage.NameCache;

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
	public List<String> autoComplete(MinecraftServer server, ICommandSender sender, String[] args)
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
			return CommandBase.getListOfStringsMatchingLastWord(args, NameCache.INSTANCE.getAllNames());
		}
		
		return list;
	}
	
	@Override
	public String getCommand()
	{
		return "reset";
	}
	
	@Override
	public void runCommand(MinecraftServer server, CommandBase command, ICommandSender sender, String[] args) throws CommandException
	{
		String action = args[1];
		
		UUID uuid = null;
		
		if(args.length == 3)
		{
			uuid = this.findPlayerID(server, sender, args[2]);
			
			if(uuid == null)
			{
				System.out.println("Can't find UUID for player \"" + args[2] + "\"");
				throw this.getException(command);
			}
			
			System.out.println("UUID for player \"" + args[2] + "\" = " + uuid.toString());
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
				sender.addChatMessage(new TextComponentTranslation("betterquesting.cmd.reset.player_all", pName));
			} else
			{
				sender.addChatMessage(new TextComponentTranslation("betterquesting.cmd.reset.all_all"));
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
					sender.addChatMessage(new TextComponentTranslation("betterquesting.cmd.reset.player_single", new TextComponentTranslation(quest.getUnlocalisedName()), pName));
				} else
				{
					quest.resetAll(true);
					sender.addChatMessage(new TextComponentTranslation("betterquesting.cmd.reset.all_single", new TextComponentTranslation(quest.getUnlocalisedName())));
				}
			} catch(Exception e)
			{
				e.printStackTrace();
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
