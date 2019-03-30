package betterquesting.commands;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.server.permission.PermissionAPI;
import betterquesting.commands.user.QuestCommandHelp;
import betterquesting.commands.user.QuestCommandRefresh;
import betterquesting.commands.user.QuestCommandSPHardcore;

public class BQ_CommandUser extends CommandBase
{
	ArrayList<QuestCommandBase> coms = new ArrayList<QuestCommandBase>();
	
	public BQ_CommandUser()
	{
		coms.add(new QuestCommandHelp());
		coms.add(new QuestCommandRefresh());
		coms.add(new QuestCommandSPHardcore());
	}
	
	@Override
	public String getName()
	{
		return "bq_user";
	}
	
	@Override
	public int getRequiredPermissionLevel() 
	{
		// TODO Auto-generated method stub
		return 0;
	}
	
	@Override
	public String getUsage(ICommandSender sender)
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
	public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] strings, BlockPos pos)
    {
		if(strings.length == 1)
		{
			ArrayList<String> base = new ArrayList<String>();
			for(QuestCommandBase c : coms)
			{
				if(PermissionAPI.hasPermission((EntityPlayer) sender, c.getPermissionNode())) 
				{
					base.add(c.getCommand());
				}				
			}
        	return getListOfStringsMatchingLastWord(strings, base.toArray(new String[0]));
		} else if(strings.length > 1)
		{
			for(QuestCommandBase c : coms)
			{
				if(c.getCommand().equalsIgnoreCase(strings[0]) && PermissionAPI.hasPermission((EntityPlayer) sender, c.getPermissionNode()))
				{
					return c.autoComplete(server, sender, strings);
				}
			}
		}
		
		return new ArrayList<String>();
    }
	
	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
	{
		if(args.length < 1)
		{
			throw new WrongUsageException(this.getUsage(sender));
		}
		
		for(QuestCommandBase c : coms)
		{
			if(c.getCommand().equalsIgnoreCase(args[0]))
			{
				if(PermissionAPI.hasPermission((EntityPlayer) sender, c.getPermissionNode())) 
				{
					if(c.validArgs(args))
					{
						c.runCommand(server, this, sender, args);
						return;
					} else
					{
						throw c.getException(this);
					}
				} else
				{
					sender.sendMessage(new TextComponentString("Not enough permission to do that !").setStyle(new Style().setColor(TextFormatting.RED)));
				}
			}
		}
		
		throw new WrongUsageException(this.getUsage(sender));
	}

    /**
     * Return whether the specified command parameter index is a username parameter.
     */
	@Override
    public boolean isUsernameIndex(String[] args, int index)
    {
		if(args.length < 1)
		{
			return false;
		}
		
		for(QuestCommandBase c : coms)
		{
			if(c.getCommand().equalsIgnoreCase(args[0]))
			{
				return c.isArgUsername(args, index);
			}
		}
		
		return false;
    }
}
