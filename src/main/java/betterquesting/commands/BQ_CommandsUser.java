package betterquesting.commands;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import betterquesting.commands.user.QuestCommandHelp;
import betterquesting.commands.user.QuestCommandRefresh;

public class BQ_CommandsUser extends CommandBase
{
	ArrayList<QuestCommandBase> coms = new ArrayList<QuestCommandBase>();
	
	public BQ_CommandsUser()
	{
		coms.add(new QuestCommandHelp());
		coms.add(new QuestCommandRefresh());
	}
	
	@Override
	public String getCommandName()
	{
		return "bq_user";
	}
	
	@Override
	public String getCommandUsage(ICommandSender sender)
	{
		String txt = "";
		
		for(int i = 0; i < coms.size(); i++)
		{
			QuestCommandBase c = coms.get(i);
			txt += "/bq_user " + c.getCommand();
			
			if(c.getUsageSuffix().length() > 0)
			{
				txt += " " + c.getUsageSuffix();
			}
			
			if(i < coms.size() -1)
			{
				txt += ", ";
			}
		}
		
		return txt;
	}

    /**
     * Adds the strings available in this command to the given list of tab completion options.
     */
	@Override
	@SuppressWarnings("unchecked")
    public List<String> addTabCompletionOptions(ICommandSender sender, String[] strings)
    {
		if(strings.length == 1)
		{
			ArrayList<String> base = new ArrayList<String>();
			for(QuestCommandBase c : coms)
			{
				base.add(c.getCommand());
			}
        	return getListOfStringsMatchingLastWord(strings, base.toArray(new String[0]));
		} else if(strings.length > 1)
		{
			for(QuestCommandBase c : coms)
			{
				if(c.getCommand().equalsIgnoreCase(strings[0]))
				{
					return c.autoComplete(sender, strings);
				}
			}
		}
		
		return new ArrayList<String>();
    }

    /**
     * Return the required permission level for this command.
     */
	@Override
    public int getRequiredPermissionLevel()
    {
        return 0;
    }
	
	@Override
	public boolean canCommandSenderUseCommand(ICommandSender sender)
	{
		return true;
	}
	
	@Override
	public void processCommand(ICommandSender sender, String[] args)
	{
		if(args.length < 1)
		{
			throw new WrongUsageException(this.getCommandUsage(sender));
		}
		
		for(QuestCommandBase c : coms)
		{
			if(c.getCommand().equalsIgnoreCase(args[0]))
			{
				if(c.validArgs(args))
				{
					c.runCommand(this, sender, args);
					return;
				} else
				{
					throw c.getException(this);
				}
			}
		}
		
		throw new WrongUsageException(this.getCommandUsage(sender));
	}
}
