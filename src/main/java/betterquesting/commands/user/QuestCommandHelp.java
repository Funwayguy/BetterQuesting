package betterquesting.commands.user;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import betterquesting.commands.QuestCommandBase;
import betterquesting.core.BetterQuesting;

public class QuestCommandHelp extends QuestCommandBase
{
	@Override
	public String getCommand()
	{
		return "help";
	}
	
	@Override
	public void runCommand(CommandBase command, ICommandSender sender, String[] args)
	{
		if(sender instanceof EntityPlayer)
		{
			EntityPlayer player = (EntityPlayer)sender;
			if(!player.inventory.addItemStackToInventory(new ItemStack(BetterQuesting.guideBook)))
			{
				player.dropPlayerItemWithRandomChoice(new ItemStack(BetterQuesting.guideBook), false);
			}
		}
	}
}
