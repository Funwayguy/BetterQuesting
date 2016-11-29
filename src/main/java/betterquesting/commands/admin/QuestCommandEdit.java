package betterquesting.commands.admin;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentTranslation;
import betterquesting.api.properties.NativeProps;
import betterquesting.commands.QuestCommandBase;
import betterquesting.network.PacketSender;
import betterquesting.storage.QuestSettings;

public class QuestCommandEdit extends QuestCommandBase
{
	@Override
	public String getCommand()
	{
		return "edit";
	}
	
	@Override
	public void runCommand(MinecraftServer server, CommandBase command, ICommandSender sender, String[] args) throws CommandException
	{
		QuestSettings.INSTANCE.setProperty(NativeProps.EDIT_MODE, !QuestSettings.INSTANCE.getProperty(NativeProps.EDIT_MODE));
		sender.addChatMessage(new TextComponentTranslation("betterquesting.cmd.edit", new TextComponentTranslation(QuestSettings.INSTANCE.getProperty(NativeProps.EDIT_MODE)? "options.on" : "options.off")));
		PacketSender.INSTANCE.sendToAll(QuestSettings.INSTANCE.getSyncPacket());
	}
}
