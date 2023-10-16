package betterquesting.commands.admin;

import betterquesting.api.properties.NativeProps;
import betterquesting.api.questing.IQuest;
import betterquesting.api2.storage.DBEntry;
import betterquesting.commands.QuestCommandBase;
import betterquesting.network.handlers.NetQuestSync;
import betterquesting.questing.QuestDatabase;
import betterquesting.storage.NameCache;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.server.permission.DefaultPermissionLevel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class QuestCommandReset extends QuestCommandBase {
  @Override
  public String getUsageSuffix() {
    return "[all|<quest_id>] [username|uuid]";
  }

  @Override
  public boolean validArgs(String[] args) {
    return args.length == 2 || args.length == 3;
  }

  @Override
  public List<String> autoComplete(MinecraftServer server, ICommandSender sender, String[] args) {
    if (args.length == 2) {
      List<String> list = new ArrayList<>();
      list.add("all");

      for (DBEntry<IQuest> i : QuestDatabase.INSTANCE.getEntries()) {
        list.add("" + i.getID());
      }

      return list;
    } else if (args.length == 3) {
      return CommandBase.getListOfStringsMatchingLastWord(args, NameCache.INSTANCE.getAllNames());
    }

    return Collections.emptyList();
  }

  @Override
  public String getCommand() {
    return "reset";
  }

  @Override
  public void runCommand(MinecraftServer server, CommandBase command, ICommandSender sender, String[] args)
      throws CommandException {
    String action = args[1];

    UUID uuid = null;

    if (args.length == 3) {
      uuid = findPlayerID(server, sender, args[2]);

      if (uuid == null) {
        throw getException(command);
      }
    }

    String pName = uuid == null ? "NULL" : NameCache.INSTANCE.getName(uuid);
    EntityPlayerMP player = uuid == null ? null : server.getPlayerList().getPlayerByUUID(uuid);

    if (action.equalsIgnoreCase("all")) {
      for (DBEntry<IQuest> entry : QuestDatabase.INSTANCE.getEntries()) {
        entry.getValue().resetUser(uuid, true); // Clear progress and state
      }

      if (uuid != null) {
        sender.sendMessage(new TextComponentTranslation("betterquesting.cmd.reset.player_all", pName));
        NetQuestSync.sendSync(player, null, false, true);
      } else {
        sender.sendMessage(new TextComponentTranslation("betterquesting.cmd.reset.all_all"));
        NetQuestSync.quickSync(-1, false, true);
      }

    } else {
      try {
        int id = Integer.parseInt(action.trim());
        IQuest quest = QuestDatabase.INSTANCE.getValue(id);

        if (uuid != null) {
          quest.resetUser(uuid, true); // Clear progress and state
          sender.sendMessage(new TextComponentTranslation("betterquesting.cmd.reset.player_single",
                                                          new TextComponentTranslation(
                                                              quest.getProperty(NativeProps.NAME)), pName));
          NetQuestSync.sendSync(player, new int[] { id }, false, true);
        } else {
          quest.resetUser(null, true);
          sender.sendMessage(new TextComponentTranslation("betterquesting.cmd.reset.all_single",
                                                          new TextComponentTranslation(
                                                              quest.getProperty(NativeProps.NAME))));
          NetQuestSync.quickSync(id, false, true);
        }
      } catch (Exception e) {
        throw getException(command);
      }
    }
  }

  @Override
  public boolean isArgUsername(String[] args, int index) {
    return index == 2;
  }

  @Override
  public String getPermissionNode() {
    return "betterquesting.command.admin.reset";
  }

  @Override
  public DefaultPermissionLevel getPermissionLevel() {
    return DefaultPermissionLevel.OP;
  }

  @Override
  public String getPermissionDescription() {
    return "Permission to erases quest completion data for the given user";
  }

}
