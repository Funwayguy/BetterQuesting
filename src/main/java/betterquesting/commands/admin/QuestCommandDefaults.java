package betterquesting.commands.admin;

import betterquesting.api.properties.NativeProps;
import betterquesting.api.storage.BQ_Settings;
import betterquesting.api.utils.JsonHelper;
import betterquesting.api.utils.NBTConverter;
import betterquesting.commands.QuestCommandBase;
import betterquesting.core.BetterQuesting;
import betterquesting.handlers.SaveLoadHandler;
import betterquesting.legacy.ILegacyLoader;
import betterquesting.legacy.LegacyLoaderRegistry;
import betterquesting.network.handlers.NetChapterSync;
import betterquesting.network.handlers.NetQuestSync;
import betterquesting.network.handlers.NetSettingSync;
import betterquesting.questing.QuestDatabase;
import betterquesting.questing.QuestLineDatabase;
import betterquesting.storage.QuestSettings;
import com.google.gson.JsonObject;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.server.permission.DefaultPermissionLevel;

import java.io.File;
import java.util.Collections;
import java.util.List;

public class QuestCommandDefaults extends QuestCommandBase {
    @Override
    public String getUsageSuffix() {
        return "[save|load|set] [file_name]";
    }

    @Override
    public boolean validArgs(String[] args) {
        return args.length == 2 || args.length == 3;
    }

    @Override
    public List<String> autoComplete(MinecraftServer server, ICommandSender sender, String[] args) {
        if (args.length == 2) {
            return CommandBase.getListOfStringsMatchingLastWord(args, "save", "load", "set");
        } else if (args.length == 3) {
            return Collections.singletonList("DefaultQuests");
        }

        return Collections.emptyList();
    }

    @Override
    public String getCommand() {
        return "default";
    }

    @Override
    public void runCommand(MinecraftServer server, CommandBase command, ICommandSender sender, String[] args) throws CommandException {
        File qFile;

        if (args.length == 3 && !args[2].equalsIgnoreCase("DefaultQuests")) {
            qFile = new File(BQ_Settings.defaultDir, "saved_quests/" + args[2] + ".json");
        } else {
            qFile = new File(BQ_Settings.defaultDir, "DefaultQuests.json");
        }

        if (args[1].equalsIgnoreCase("save")) {
            boolean editMode = QuestSettings.INSTANCE.getProperty(NativeProps.EDIT_MODE);

            NBTTagCompound base = new NBTTagCompound();

            QuestSettings.INSTANCE.setProperty(NativeProps.EDIT_MODE, false);
            base.setTag("questSettings", QuestSettings.INSTANCE.writeToNBT(new NBTTagCompound()));
            QuestSettings.INSTANCE.setProperty(NativeProps.EDIT_MODE, editMode);
            base.setTag("questDatabase", QuestDatabase.INSTANCE.writeToNBT(new NBTTagList(), null));
            base.setTag("questLines", QuestLineDatabase.INSTANCE.writeToNBT(new NBTTagList(), null));
            base.setString("format", BetterQuesting.FORMAT);
            JsonHelper.WriteToFile(qFile, NBTConverter.NBTtoJSON_Compound(base, new JsonObject(), true));

            if (args.length == 3 && !args[2].equalsIgnoreCase("DefaultQuests")) {
                sender.sendMessage(new TextComponentTranslation("betterquesting.cmd.default.save2", args[2] + ".json"));
            } else {
                sender.sendMessage(new TextComponentTranslation("betterquesting.cmd.default.save"));
            }
        } else if (args[1].equalsIgnoreCase("load")) {
            if (qFile.exists()) {
                boolean editMode = QuestSettings.INSTANCE.getProperty(NativeProps.EDIT_MODE);
                boolean hardMode = QuestSettings.INSTANCE.getProperty(NativeProps.HARDCORE);

                NBTTagList jsonP = QuestDatabase.INSTANCE.writeProgressToNBT(new NBTTagList(), null);

                JsonObject j1 = JsonHelper.ReadFromFile(qFile);
                NBTTagCompound nbt1 = NBTConverter.JSONtoNBT_Object(j1, new NBTTagCompound(), true);

                ILegacyLoader loader = LegacyLoaderRegistry.getLoader(nbt1.hasKey("format", 8) ? nbt1.getString("format") : "0.0.0");

                if (loader == null) {
                    QuestSettings.INSTANCE.readFromNBT(nbt1.getCompoundTag("questSettings"));
                    QuestDatabase.INSTANCE.readFromNBT(nbt1.getTagList("questDatabase", 10), false);
                    QuestLineDatabase.INSTANCE.readFromNBT(nbt1.getTagList("questLines", 10), false);
                } else {
                    loader.readFromJson(j1);
                }

                QuestDatabase.INSTANCE.readProgressFromNBT(jsonP, false);

                QuestSettings.INSTANCE.setProperty(NativeProps.EDIT_MODE, editMode);
                QuestSettings.INSTANCE.setProperty(NativeProps.HARDCORE, hardMode);

                if (args.length == 3 && !args[2].equalsIgnoreCase("DefaultQuests")) {
                    sender.sendMessage(new TextComponentTranslation("betterquesting.cmd.default.load2", args[2] + ".json"));
                } else {
                    sender.sendMessage(new TextComponentTranslation("betterquesting.cmd.default.load"));
                }

                NetSettingSync.sendSync(null);
                NetQuestSync.quickSync(-1, true, true);
                NetChapterSync.sendSync(null, null);
                SaveLoadHandler.INSTANCE.markDirty();
            } else {
                sender.sendMessage(new TextComponentTranslation("betterquesting.cmd.default.none"));
            }
        } else if (args[1].equalsIgnoreCase("set") && args.length == 3) {
            if (qFile.exists() && !args[2].equalsIgnoreCase("DefaultQuests")) {
                File defFile = new File(BQ_Settings.defaultDir, "DefaultQuests.json");

                if (defFile.exists()) {
                    defFile.delete();
                }

                JsonHelper.CopyPaste(qFile, defFile);

                sender.sendMessage(new TextComponentTranslation("betterquesting.cmd.default.set", args[2]));
            } else {
                sender.sendMessage(new TextComponentTranslation("betterquesting.cmd.default.none"));
            }
        } else {
            throw getException(command);
        }
    }

    @Override
    public String getPermissionNode() {
        return "betterquesting.command.admin.default";
    }

    @Override
    public DefaultPermissionLevel getPermissionLevel() {
        return DefaultPermissionLevel.OP;
    }

    @Override
    public String getPermissionDescription() {
        return "Permission to saves/loads the current quest database to/from the global default directory";
    }
}
