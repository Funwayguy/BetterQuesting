package betterquesting.commands.admin;

import betterquesting.commands.QuestCommandBase;
import betterquesting.quests.QuestDatabase;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;

public class QuestCommandEdit extends QuestCommandBase
{
	@Override
	public String getCommand()
	{
		return "edit";
	}
	
	@Override
	public void runCommand(CommandBase command, ICommandSender sender, String[] args)
	{
		QuestDatabase.editMode = !QuestDatabase.editMode;
		QuestDatabase.UpdateClients();
		sender.addChatMessage(new ChatComponentText("Edit mode " + (QuestDatabase.editMode? "enabled" : "disabled")));
	}
}
