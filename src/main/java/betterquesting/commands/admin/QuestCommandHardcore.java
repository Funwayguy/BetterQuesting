package betterquesting.commands.admin;

import betterquesting.api.properties.NativeProps;
import betterquesting.commands.QuestCommandBase;
import betterquesting.handlers.SaveLoadHandler;
import betterquesting.network.handlers.NetSettingSync;
import betterquesting.storage.QuestSettings;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.server.permission.DefaultPermissionLevel;

import java.util.Collections;
import java.util.List;

public class QuestCommandHardcore extends QuestCommandBase {
    @Override
    public String getCommand() {
        return "hardcore";
    }

    @Override
    public String getUsageSuffix() {
        return "[true|false]";
    }

    @Override
    public boolean validArgs(String[] args) {
        return args.length == 1 || args.length == 2;
    }

    @Override
    public List<String> autoComplete(MinecraftServer server, ICommandSender sender, String[] args) {
        return args.length == 2 ? CommandBase.getListOfStringsMatchingLastWord(args, "true", "false") : Collections.emptyList();
    }

    @Override
    public void runCommand(MinecraftServer server, CommandBase command, ICommandSender sender, String[] args) throws CommandException {
        boolean flag = !QuestSettings.INSTANCE.getProperty(NativeProps.HARDCORE);

        if (args.length == 2) {
            try {
                if (args[1].equalsIgnoreCase("on")) {
                    flag = true;
                } else if (args[1].equalsIgnoreCase("off")) {
                    flag = false;
                } else {
                    flag = Boolean.parseBoolean(args[1]);
                }
            } catch (Exception e) {
                throw this.getException(command);
            }
        }

        QuestSettings.INSTANCE.setProperty(NativeProps.HARDCORE, flag);
        SaveLoadHandler.INSTANCE.markDirty();

        sender.sendMessage(new TextComponentTranslation("betterquesting.cmd.hardcore", new TextComponentTranslation(QuestSettings.INSTANCE.getProperty(NativeProps.HARDCORE) ? "options.on" : "options.off")));
        NetSettingSync.sendSync(null);
    }

    @Override
    public String getPermissionNode() {
        return "betterquesting.command.admin.hardcore";
    }

    @Override
    public DefaultPermissionLevel getPermissionLevel() {
        return DefaultPermissionLevel.OP;
    }

    @Override
    public String getPermissionDescription() {
        return "Permission to activate or not the use of hardcore lives";
    }
}
