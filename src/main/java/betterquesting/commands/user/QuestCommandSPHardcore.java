package betterquesting.commands.user;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.EnumChatFormatting;
import betterquesting.api.properties.NativeProps;
import betterquesting.commands.QuestCommandBase;
import betterquesting.network.PacketSender;
import betterquesting.storage.QuestSettings;

public class QuestCommandSPHardcore extends QuestCommandBase
{
	@Override
	public String getCommand()
	{
		return "harcore";
	}
	
	@Override
	public void runCommand(CommandBase command, ICommandSender sender, String[] args)
	{
		MinecraftServer server = MinecraftServer.getServer();
		
		if(!server.isSinglePlayer() || !server.getServerOwner().equalsIgnoreCase(sender.getCommandSenderName()))
		{
			ChatComponentTranslation cc = new ChatComponentTranslation("commands.generic.permission");
			cc.getChatStyle().setColor(EnumChatFormatting.RED);
			sender.addChatMessage(cc);
			return;
		}
		
		QuestSettings.INSTANCE.setProperty(NativeProps.HARDCORE, true);
		sender.addChatMessage(new ChatComponentTranslation("betterquesting.cmd.hardcore", new ChatComponentTranslation("options.on")));
		PacketSender.INSTANCE.sendToAll(QuestSettings.INSTANCE.getSyncPacket());
	}
}
