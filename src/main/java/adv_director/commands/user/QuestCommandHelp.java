package adv_director.commands.user;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import adv_director.commands.QuestCommandBase;
import adv_director.core.AdvDirector;

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
			if(!player.inventory.addItemStackToInventory(new ItemStack(AdvDirector.guideBook)))
			{
				player.dropItem(new ItemStack(AdvDirector.guideBook), true, false);
			}
		}
	}
}
