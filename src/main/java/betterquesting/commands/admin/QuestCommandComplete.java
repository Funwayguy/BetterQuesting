package betterquesting.commands.admin;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentTranslation;
import betterquesting.api.quests.IQuest;
import betterquesting.api.quests.properties.NativeProps;
import betterquesting.api.quests.tasks.ITask;
import betterquesting.commands.QuestCommandBase;
import betterquesting.database.QuestDatabase;
import betterquesting.network.PacketSender;

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
	
	@SuppressWarnings("unchecked")
	public List<String> autoComplete(ICommandSender sender, String[] args)
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
			return CommandBase.getListOfStringsMatchingLastWord(args, MinecraftServer.getServer().getAllUsernames());
		}
		
		return list;
	}
	
	@Override
	public String getCommand()
	{
		return "complete";
	}
	
	@Override
	public void runCommand(CommandBase command, ICommandSender sender, String[] args)
	{
		UUID uuid = null;
		EntityPlayerMP player = null;
		
		player = MinecraftServer.getServer().getConfigurationManager().func_152612_a(args[2]);
		
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
			uuid = player.getGameProfile().getId();
		}
		
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
			
			sender.addChatMessage(new ChatComponentTranslation("betterquesting.cmd.complete", new ChatComponentTranslation(quest.getUnlocalisedName()), player.getCommandSenderName()));
		} catch(Exception e)
		{
			throw getException(command);
		}
		
		PacketSender.INSTANCE.sendToAll(QuestDatabase.INSTANCE.getSyncPacket());
	}
}
