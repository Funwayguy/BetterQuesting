package betterquesting.commands.admin;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentTranslation;
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
	public void runCommand(MinecraftServer server, CommandBase command, ICommandSender sender, String[] args)
	{
		QuestDatabase.bqHardcore = !QuestDatabase.bqHardcore;
		sender.addChatMessage(new TextComponentTranslation("betterquesting.cmd.hardcore", new TextComponentTranslation(QuestDatabase.bqHardcore? "options.on" : "options.off")));
		QuestDatabase.UpdateClients();
	}
}
