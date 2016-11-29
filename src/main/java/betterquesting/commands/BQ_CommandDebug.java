package betterquesting.commands;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;

public class BQ_CommandDebug extends CommandBase
{
	@Override
	public String getCommandName()
	{
		return "bq_debug";
	}
	
	@Override
	public String getCommandUsage(ICommandSender sender)
	{
		return "TO BE USED IN DEV ONLY";
	}
	
	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
	{
	}
}
