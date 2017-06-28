package adv_director.commands.user;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentTranslation;
import adv_director.commands.QuestCommandBase;
import adv_director.network.PacketSender;
import adv_director.questing.QuestDatabase;
import adv_director.questing.QuestLineDatabase;
import adv_director.storage.LifeDatabase;
import adv_director.storage.NameCache;
import adv_director.storage.QuestSettings;

public class QuestCommandRefresh extends QuestCommandBase
{
	@Override
	public String getCommand()
	{
		return "refresh";
	}
	
	@Override
	public void runCommand(MinecraftServer server, CommandBase command, ICommandSender sender, String[] args) throws CommandException
	{
		if(sender instanceof EntityPlayerMP)
		{
			EntityPlayerMP player = (EntityPlayerMP)sender;
			PacketSender.INSTANCE.sendToPlayer(QuestDatabase.INSTANCE.getSyncPacket(), player);
			PacketSender.INSTANCE.sendToPlayer(QuestLineDatabase.INSTANCE.getSyncPacket(), player);
			PacketSender.INSTANCE.sendToPlayer(LifeDatabase.INSTANCE.getSyncPacket(), player);
			PacketSender.INSTANCE.sendToPlayer(NameCache.INSTANCE.getSyncPacket(), player);
			PacketSender.INSTANCE.sendToPlayer(QuestSettings.INSTANCE.getSyncPacket(), player);
			sender.addChatMessage(new TextComponentTranslation("betterquesting.cmd.refresh"));
		}
	}
}
