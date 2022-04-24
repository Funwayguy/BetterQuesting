package betterquesting.commands.user;

import betterquesting.commands.QuestCommandBase;
import betterquesting.core.BetterQuesting;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.server.permission.DefaultPermissionLevel;

public class QuestCommandHelp extends QuestCommandBase {
    @Override
    public String getCommand() {
        return "help";
    }

    @Override
    public void runCommand(MinecraftServer server, CommandBase command, ICommandSender sender, String[] args) throws CommandException {
        if (sender instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) sender;
            if (!player.inventory.addItemStackToInventory(new ItemStack(BetterQuesting.guideBook))) {
                player.dropItem(new ItemStack(BetterQuesting.guideBook), true, false);
            }
        }
    }

    @Override
    public String getPermissionNode() {
        return "betterquesting.command.user.help";
    }

    @Override
    public DefaultPermissionLevel getPermissionLevel() {
        return DefaultPermissionLevel.ALL;
    }

    @Override
    public String getPermissionDescription() {
        return "Permission to execute command which gives the player a copy of the in game starter guide.";
    }

}
