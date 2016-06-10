package betterquesting.commands.admin;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentTranslation;
import betterquesting.commands.QuestCommandBase;
import betterquesting.quests.QuestDatabase;

public class QuestCommandHardcore extends QuestCommandBase
{
	@Override
	public String getCommand()
	{
		return "hardcore";
	}
	
	@Override
	public void runCommand(CommandBase command, ICommandSender sender, String[] args)
	{
		QuestDatabase.bqHardcore = !QuestDatabase.bqHardcore;
		sender.addChatMessage(new ChatComponentTranslation("betterquesting.cmd.hardcore", new ChatComponentTranslation(QuestDatabase.bqHardcore? "options.on" : "options.off")));
		QuestDatabase.UpdateClients();
	}
}
