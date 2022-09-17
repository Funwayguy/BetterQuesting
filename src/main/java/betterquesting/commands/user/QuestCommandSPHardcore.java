package betterquesting.commands.user;

import betterquesting.api.properties.NativeProps;
import betterquesting.commands.QuestCommandBase;
import betterquesting.handlers.SaveLoadHandler;
import betterquesting.network.handlers.NetSettingSync;
import betterquesting.storage.QuestSettings;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.EnumChatFormatting;

public class QuestCommandSPHardcore extends QuestCommandBase
{
	@Override
	public String getCommand()
	{
		return "hardcore";
	}
	
	@Override
	public void runCommand(MinecraftServer server, CommandBase command, ICommandSender sender, String[] args)
	{
		if(!server.isSinglePlayer() || !server.getServerOwner().equalsIgnoreCase(sender.getCommandSenderName()))
		{
			ChatComponentTranslation cc = new ChatComponentTranslation("commands.generic.permission");
			cc.getChatStyle().setColor(EnumChatFormatting.RED);
			sender.addChatMessage(cc);
			return;
		}
		
		QuestSettings.INSTANCE.setProperty(NativeProps.HARDCORE, true);
        SaveLoadHandler.INSTANCE.saveDatabases();
        
		sender.addChatMessage(new ChatComponentTranslation("betterquesting.cmd.hardcore", new ChatComponentTranslation("options.on")));
		NetSettingSync.sendSync(null);
	}
}
