package betterquesting.commands.admin;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentTranslation;
import betterquesting.commands.QuestCommandBase;
import betterquesting.network.PacketSender;
import betterquesting.quests.QuestSettings;

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
		QuestSettings.INSTANCE.setEditMode(!QuestSettings.INSTANCE.isEditMode());
		sender.addChatMessage(new ChatComponentTranslation("betterquesting.cmd.edit", new ChatComponentTranslation(QuestSettings.INSTANCE.isEditMode()? "options.on" : "options.off")));
		PacketSender.INSTANCE.sendToAll(QuestSettings.INSTANCE.getSyncPacket());
	}
}
