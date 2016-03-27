package betterquesting.commands.admin;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.translation.I18n;
import betterquesting.commands.QuestCommandBase;
import betterquesting.quests.QuestDatabase;
import betterquesting.quests.QuestInstance;

public class QuestCommandComplete extends QuestCommandBase
{
	public String getUsageSuffix()
	{
		return "<quest_id> [username|uuid]";
	}
	
	public boolean validArgs(String[] args)
	{
		return args.length == 3;
	}
	
	public List<String> autoComplete(MinecraftServer server, ICommandSender sender, String[] args)
	{
		ArrayList<String> list = new ArrayList<String>();
		
		if(args.length == 2)
		{
			for(int i : QuestDatabase.questDB.keySet())
			{
				list.add("" + i);
			}
		} else if(args.length == 3)
		{
			return CommandBase.getListOfStringsMatchingLastWord(args, sender.getEntityWorld().getMinecraftServer().getAllUsernames());
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
		EntityPlayerMP player = null;
		
		player = server.getPlayerList().getPlayerByUsername(args[2]);
		
		if(player == null)
		{
			try
			{
				uuid = UUID.fromString(args[2]);
			} catch(Exception e)
			{
				throw getException(command);
			}
		} else
		{
			uuid = player.getUniqueID();
		}
		
		try
		{
			int id = Integer.parseInt(args[1].trim());
			QuestInstance quest = QuestDatabase.getQuestByID(id);
			quest.setComplete(uuid, 0);
			
			sender.addChatMessage(new TextComponentString("Manually completed quest " + I18n.translateToLocalFormatted(quest.name) +"(ID:" + id + ")" + (player != null? " for " + player.getName() : (uuid != null? " for " + uuid.toString() : ""))));
		} catch(Exception e)
		{
			throw getException(command);
		}
		
		QuestDatabase.UpdateClients();
	}
}
