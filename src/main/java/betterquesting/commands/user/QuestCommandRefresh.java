package betterquesting.commands.user;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ChatComponentTranslation;
import betterquesting.commands.QuestCommandBase;
import betterquesting.lives.LifeDatabase;
import betterquesting.network.PacketSender;
import betterquesting.quests.NameCache;
import betterquesting.quests.QuestDatabase;
import betterquesting.quests.QuestLineDatabase;

public class QuestCommandRefresh extends QuestCommandBase
{
	@Override
	public String getCommand()
	{
		return "refresh";
	}
	
	@Override
	public void runCommand(CommandBase command, ICommandSender sender, String[] args)
	{
		if(sender instanceof EntityPlayerMP)
		{
			EntityPlayerMP player = (EntityPlayerMP)sender;
			PacketSender.INSTANCE.sendToPlayer(QuestDatabase.INSTANCE.getSyncPacket(), player);
			PacketSender.INSTANCE.sendToPlayer(QuestLineDatabase.INSTANCE.getSyncPacket(), player);
			PacketSender.INSTANCE.sendToPlayer(LifeDatabase.INSTANCE.getSyncPacket(), player);
			PacketSender.INSTANCE.sendToPlayer(NameCache.INSTANCE.getSyncPacket(), player);
			sender.addChatMessage(new ChatComponentTranslation("betterquesting.cmd.refresh"));
		}
	}
}
