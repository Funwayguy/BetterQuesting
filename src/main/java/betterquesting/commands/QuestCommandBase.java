package betterquesting.commands;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;

public abstract class QuestCommandBase
{
	public abstract String getCommand();
	
	public String getUsageSuffix()
	{
		return "";
	}
	
	/**
	 * Are the passed arguments valid?<br>
	 * NOTE: Argument 1 is always the returned value of getCommand()
	 */
	public boolean validArgs(String[] args)
	{
		return args.length == 1;
	}
	
	public List<String> autoComplete(ICommandSender sender, String[] args)
	{
		return new ArrayList<String>();
	}
	
	public abstract void runCommand(CommandBase command, ICommandSender sender, String[] args);
	
	public final WrongUsageException getException(CommandBase command)
	{
		String message = command.getCommandName() + " " + getCommand();
		
		if(getUsageSuffix().length() > 0)
		{
			message += " " + getUsageSuffix();
		}
		
		return new WrongUsageException(message);
	}
}
