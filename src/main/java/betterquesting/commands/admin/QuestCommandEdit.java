package betterquesting.commands.admin;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentTranslation;
import betterquesting.api.properties.NativeProps;
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
		QuestSettings.INSTANCE.setProperty(NativeProps.EDIT_MODE, !QuestSettings.INSTANCE.getProperty(NativeProps.EDIT_MODE));
		sender.addChatMessage(new ChatComponentTranslation("betterquesting.cmd.edit", new ChatComponentTranslation(QuestSettings.INSTANCE.getProperty(NativeProps.EDIT_MODE)? "options.on" : "options.off")));
		PacketSender.INSTANCE.sendToAll(QuestSettings.INSTANCE.getSyncPacket());
	}
}
