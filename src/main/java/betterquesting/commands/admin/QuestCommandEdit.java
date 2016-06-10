package betterquesting.commands.admin;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentTranslation;
import betterquesting.commands.QuestCommandBase;
import betterquesting.quests.QuestDatabase;

public class QuestCommandEdit extends QuestCommandBase
{
	@Override
	public String getCommand()
	{
		return "edit";
	}
	
	@Override
	public void runCommand(MinecraftServer server, CommandBase command, ICommandSender sender, String[] args)
	{
		QuestDatabase.editMode = !QuestDatabase.editMode;
		sender.addChatMessage(new TextComponentTranslation("betterquesting.cmd.edit", new TextComponentTranslation(QuestDatabase.editMode? "options.on" : "options.off")));
		QuestDatabase.UpdateClients();
	}
}
