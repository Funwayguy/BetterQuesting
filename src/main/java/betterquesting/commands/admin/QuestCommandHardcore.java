package betterquesting.commands.admin;

import betterquesting.commands.QuestCommandBase;
import betterquesting.quests.QuestDatabase;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;

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
		QuestDatabase.UpdateClients();
		sender.addChatMessage(new ChatComponentText("Hardcore mode " + (QuestDatabase.bqHardcore? "enabled" : "disabled")));
	}
}
