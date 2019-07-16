package betterquesting.commands.user;

import betterquesting.commands.QuestCommandBase;
import betterquesting.network.handlers.NetBulkSync;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.server.permission.DefaultPermissionLevel;

public class QuestCommandRefresh extends QuestCommandBase
{
	@Override
	public String getCommand()
	{
		return "refresh";
	}
	
	@Override
	public void runCommand(MinecraftServer server, CommandBase command, ICommandSender sender, String[] args)
	{
	    if(!(sender instanceof EntityPlayerMP)) return;
        EntityPlayerMP player = (EntityPlayerMP)sender;
	    
		if(server.isDedicatedServer() || !server.getServerOwner().equals(player.getGameProfile().getName()))
		{
            NetBulkSync.sendReset(player, true, true);
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
		return "Permission to manually resync the local questing database with the server in case of potential desync issues";
	}
	
}
