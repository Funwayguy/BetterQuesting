package adv_director.commands.user;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import adv_director.api.properties.NativeProps;
import adv_director.commands.QuestCommandBase;
import adv_director.network.PacketSender;
import adv_director.storage.QuestSettings;

public class QuestCommandSPHardcore extends QuestCommandBase
{
	@Override
	public String getCommand()
	{
		return "hardcore";
	}
	
	@Override
	public void runCommand(MinecraftServer server, CommandBase command, ICommandSender sender, String[] args) throws CommandException
	{
		if(!server.isSinglePlayer() || !server.getServerOwner().equalsIgnoreCase(sender.getName()))
		{
			TextComponentTranslation cc = new TextComponentTranslation("commands.generic.permission");
			cc.getStyle().setColor(TextFormatting.RED);
			sender.addChatMessage(cc);
			return;
		}
		
		QuestSettings.INSTANCE.setProperty(NativeProps.HARDCORE, true);
		sender.addChatMessage(new TextComponentTranslation("betterquesting.cmd.hardcore", new TextComponentTranslation("options.on")));
		PacketSender.INSTANCE.sendToAll(QuestSettings.INSTANCE.getSyncPacket());
	}
}
