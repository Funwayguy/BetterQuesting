package betterquesting.commands;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;

public class BQ_CommandsDebug extends CommandBase
{
	// Used purely for testing stuffs
	
	@Override
	public String getCommandName()
	{
		return "bq_debug";
	}
	
	@Override
	public String getCommandUsage(ICommandSender p_71518_1_)
	{
		return "/bq_debug";
	}

    /**
     * Return the required permission level for this command.
     */
    public int getRequiredPermissionLevel()
    {
        return 0;
    }
	
	@Override
	public void processCommand(ICommandSender sender, String[] args)
	{
	}
}
