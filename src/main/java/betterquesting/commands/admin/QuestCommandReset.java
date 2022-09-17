package betterquesting.commands.admin;

import betterquesting.api.properties.NativeProps;
import betterquesting.api.questing.IQuest;
import betterquesting.api2.storage.DBEntry;
import betterquesting.commands.QuestCommandBase;
import betterquesting.handlers.SaveLoadHandler;
import betterquesting.network.handlers.NetQuestSync;
import betterquesting.questing.QuestDatabase;
import betterquesting.storage.NameCache;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentTranslation;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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
	public List<String> autoComplete(MinecraftServer server, ICommandSender sender, String[] args)
	{
		ArrayList<String> list = new ArrayList<>();
		
		if(args.length == 2)
		{
			list.add("all");
			
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
		return "reset";
	}
	
	@Override
	public void runCommand(MinecraftServer server, CommandBase command, ICommandSender sender, String[] args)
	{
		String action = args[1];
		
		UUID uuid = null;
		
		if(args.length == 3)
		{
			uuid = this.findPlayerID(server, sender, args[2]);
			
			if(uuid == null)
			{
				throw this.getException(command);
			}
		}
		
		String pName = uuid == null? "NULL" : NameCache.INSTANCE.getName(uuid);
        EntityPlayerMP player = null;
        if(uuid != null)
        {
            for(EntityPlayerMP p : (List<EntityPlayerMP>)server.getConfigurationManager().playerEntityList)
            {
                if(p.getGameProfile().getId().equals(uuid))
                {
                    player = p;
                    break;
                }
            }
        }
		
		if(action.equalsIgnoreCase("all"))
		{
			for(DBEntry<IQuest> entry : QuestDatabase.INSTANCE.getEntries())
			{
				if(uuid != null)
				{
					entry.getValue().resetUser(uuid, true); // Clear progress and state
				} else
				{
					entry.getValue().resetUser(null, true);
				}
			}

			SaveLoadHandler.INSTANCE.markDirty();
			
			if(uuid != null)
			{
				sender.addChatMessage(new ChatComponentTranslation("betterquesting.cmd.reset.player_all", pName));
				if(player != null) NetQuestSync.sendSync(player, null, false, true, true);
			} else
			{
				sender.addChatMessage(new ChatComponentTranslation("betterquesting.cmd.reset.all_all"));
                NetQuestSync.quickSync(-1, false, true);
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
					SaveLoadHandler.INSTANCE.markDirty();
					sender.addChatMessage(new ChatComponentTranslation("betterquesting.cmd.reset.player_single", new ChatComponentTranslation(quest.getProperty(NativeProps.NAME)), pName));
					if(player != null) NetQuestSync.sendSync(player, new int[]{id}, false, true, true);
				} else
				{
					quest.resetUser(null, true);
					SaveLoadHandler.INSTANCE.markDirty();
					sender.addChatMessage(new ChatComponentTranslation("betterquesting.cmd.reset.all_single", new ChatComponentTranslation(quest.getProperty(NativeProps.NAME))));
					NetQuestSync.quickSync(id, false, true);
				}
			} catch(Exception e)
			{
				throw getException(command);
			}
		}
	}
	
	@Override
	public boolean isArgUsername(String[] args, int index)
	{
		return index == 2;
	}
}
