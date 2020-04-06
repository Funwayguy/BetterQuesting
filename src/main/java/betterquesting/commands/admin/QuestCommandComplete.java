package betterquesting.commands.admin;

import betterquesting.api.properties.NativeProps;
import betterquesting.api.questing.IQuest;
import betterquesting.api2.storage.DBEntry;
import betterquesting.commands.QuestCommandBase;
import betterquesting.network.handlers.NetQuestEdit;
import betterquesting.questing.QuestDatabase;
import betterquesting.storage.NameCache;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentTranslation;

import java.util.ArrayList;
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
	@SuppressWarnings("unchecked")
	public List<String> autoComplete(MinecraftServer server, ICommandSender sender, String[] args)
	{
		ArrayList<String> list = new ArrayList<>();
		
		if(args.length == 2)
		{
			for(DBEntry<IQuest> i : QuestDatabase.INSTANCE.getEntries())
			{
				list.add("" + i.getID());
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
			uuid = this.findPlayerID(server, sender, sender.getCommandSenderName());
		}
		
		String pName = uuid == null? "NULL" : NameCache.INSTANCE.getName(uuid);
		
        int id = Integer.parseInt(args[1].trim());
        IQuest quest = QuestDatabase.INSTANCE.getValue(id);
        if(quest == null) throw getException(command);
        NetQuestEdit.setQuestStates(new int[]{id}, true, uuid);
        sender.addChatMessage(new ChatComponentTranslation("betterquesting.cmd.complete", new ChatComponentTranslation(quest.getProperty(NativeProps.NAME)), pName));
	}
	
	@Override
	public boolean isArgUsername(String[] args, int index)
	{
		return index == 2;
	}
}
