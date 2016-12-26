package betterquesting.commands.user;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
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
	public void runCommand(MinecraftServer server, CommandBase command, ICommandSender sender, String[] args) throws CommandException
	{
		if(sender instanceof EntityPlayer)
		{
			EntityPlayer player = (EntityPlayer)sender;
			if(!player.inventory.addItemStackToInventory(new ItemStack(BetterQuesting.guideBook)))
			{
				player.dropItem(new ItemStack(BetterQuesting.guideBook), true, false);
			}
		}
	}
}
