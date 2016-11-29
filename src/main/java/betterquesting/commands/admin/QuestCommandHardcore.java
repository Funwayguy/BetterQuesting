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

public class QuestCommandHardcore extends QuestCommandBase
{
	@Override
	public String getCommand()
	{
		return "hardcore";
	}
	
	@Override
	public void runCommand(MinecraftServer server, CommandBase command, ICommandSender sender, String[] args) throws CommandException
	{
		QuestSettings.INSTANCE.setProperty(NativeProps.HARDCORE, !QuestSettings.INSTANCE.getProperty(NativeProps.HARDCORE));
		sender.addChatMessage(new TextComponentTranslation("betterquesting.cmd.hardcore", new TextComponentTranslation(QuestSettings.INSTANCE.getProperty(NativeProps.HARDCORE)? "options.on" : "options.off")));
		PacketSender.INSTANCE.sendToAll(QuestSettings.INSTANCE.getSyncPacket());
	}
}
