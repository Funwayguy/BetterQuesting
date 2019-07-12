package betterquesting.commands.user;

import betterquesting.api.api.QuestingAPI;
import betterquesting.commands.QuestCommandBase;
import betterquesting.network.handlers.NetChapterSync;
import betterquesting.network.handlers.NetLifeSync;
import betterquesting.network.handlers.NetQuestSync;
import betterquesting.network.handlers.NetSettingSync;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.server.permission.DefaultPermissionLevel;

import java.util.UUID;

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
			UUID playerID = QuestingAPI.getQuestingUUID(player);
            NetQuestSync.sendSync(player, null, true, true);
            NetChapterSync.sendSync(player, null);
            NetLifeSync.sendSync(new EntityPlayerMP[]{player}, new UUID[]{playerID});
			//PacketSender.INSTANCE.sendToPlayers(PktHandlerNameCache.INSTANCE.getSyncPacket(null), player); // TODO: Determine if this is really necessary client side. There could be hundreds of names
            NetSettingSync.sendSync(player);
			sender.sendMessage(new TextComponentTranslation("betterquesting.cmd.refresh"));
		}
	}
	
	@Override
	public String getPermissionNode() 
	{
		return "betterquesting.command.user.refresh";
	}

	@Override
	public DefaultPermissionLevel getPermissionLevel() 
	{
		return DefaultPermissionLevel.ALL;
	}

	@Override
	public String getPermissionDescription() 
	{
		return "Permission to manually resyncs the local questing database with the server in case of potential desync issues";
	}
	
}
