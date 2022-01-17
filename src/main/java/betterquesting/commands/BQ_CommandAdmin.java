package betterquesting.commands;

import betterquesting.commands.admin.*;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.server.permission.DefaultPermissionLevel;
import net.minecraftforge.server.permission.PermissionAPI;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BQ_CommandAdmin extends CommandBase
{
	private final List<QuestCommandBase> coms = new ArrayList<>();
	
	public BQ_CommandAdmin()
	{
		PermissionAPI.registerNode("betterquesting.command.admin", DefaultPermissionLevel.OP, "admin commmand permission");
		
		coms.add(new QuestCommandEdit());
		coms.add(new QuestCommandHardcore());
		coms.add(new QuestCommandReset());
		coms.add(new QuestCommandComplete());
		coms.add(new QuestCommandDelete());
		coms.add(new QuestCommandDefaults());
		coms.add(new QuestCommandLives());
		coms.add(new QuestCommandPurge());
		coms.add(new QuestCommandCheckCompletion());
		coms.add(new QuestCommandReportAllProgress());
	}
	
	@Override
	public String getName()
	{
		return "bq_admin";
	}
	
	@Override
	public boolean checkPermission(MinecraftServer server, ICommandSender sender)
	{
        return !(sender instanceof EntityPlayer) || PermissionAPI.hasPermission((EntityPlayer)sender, "betterquesting.command.admin");
	}
	
	@Override
	public String getUsage(ICommandSender sender)
	{
		StringBuilder txt = new StringBuilder();
		
		for(int i = 0; i < coms.size(); i++)
		{
			QuestCommandBase c = coms.get(i);
			txt.append("/bq_admin ").append(c.getCommand());
			
			if(c.getUsageSuffix().length() > 0)
			{
				txt.append(" ").append(c.getUsageSuffix());
			}
			
			if(i < coms.size() -1)
			{
				txt.append(", ");
			}
		}
		
		return txt.toString();
	}

    /**
     * Adds the strings available in this command to the given list of tab completion options.
     */
	@Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] strings, BlockPos pos)
    {
		if(strings.length == 1)
		{
			List<String> base = new ArrayList<>();
			for(QuestCommandBase c : coms)
			{
				if(!(sender instanceof EntityPlayer) || PermissionAPI.hasPermission((EntityPlayer) sender, c.getPermissionNode())) 
				{
					base.add(c.getCommand());
				}				
			}
        	return getListOfStringsMatchingLastWord(strings, base.toArray(new String[0]));
		} else if(strings.length > 1)
		{
			for(QuestCommandBase c : coms)
			{
				if(c.getCommand().equalsIgnoreCase(strings[0]))
				{
					if(!(sender instanceof EntityPlayer) || PermissionAPI.hasPermission((EntityPlayer) sender, c.getPermissionNode()))
					{
						return c.autoComplete(server, sender, strings);
					}
				}
			}
		}
		
		return Collections.emptyList();
    }
	
	@Override
    public int getRequiredPermissionLevel()
    {
        return 2;
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
				if(!(sender instanceof EntityPlayer) || PermissionAPI.hasPermission((EntityPlayer) sender, c.getPermissionNode())) 
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
					TextComponentTranslation cc = new TextComponentTranslation("commands.generic.permission");
					cc.getStyle().setColor(TextFormatting.RED);
					sender.sendMessage(cc);
					return;
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
