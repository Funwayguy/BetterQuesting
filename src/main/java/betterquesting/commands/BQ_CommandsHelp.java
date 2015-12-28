package betterquesting.commands;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import betterquesting.core.BetterQuesting;

public class BQ_CommandsHelp extends CommandBase
{
	
	@Override
	public String getCommandName()
	{
		return "bq_help";
	}
	
	@Override
	public String getCommandUsage(ICommandSender p_71518_1_)
	{
		return "/bq_help";
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
		if(sender instanceof EntityPlayer)
		{
			((EntityPlayer)sender).inventory.addItemStackToInventory(new ItemStack(BetterQuesting.guideBook));
		}
	}
}
