package betterquesting.commands.admin;

import betterquesting.api.questing.IQuest;
import betterquesting.api.questing.IQuestLine;
import betterquesting.api.questing.IQuestLineEntry;
import betterquesting.api2.storage.DBEntry;
import betterquesting.commands.QuestCommandBase;
import betterquesting.network.handlers.NetQuestEdit;
import betterquesting.questing.QuestDatabase;
import betterquesting.questing.QuestLineDatabase;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.server.permission.DefaultPermissionLevel;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

public class QuestCommandPurge extends QuestCommandBase {
    @Override
    public String getCommand() {
        return "purge_hidden_quests";
    }

    @Override
    public void runCommand(MinecraftServer server, CommandBase command, ICommandSender sender, String[] args) {
        TreeSet<Integer> knownKeys = new TreeSet<>();

        for (DBEntry<IQuestLine> entry : QuestLineDatabase.INSTANCE.getEntries()) {
            for (DBEntry<IQuestLineEntry> qle : entry.getValue().getEntries()) {
                knownKeys.add(qle.getID());
            }
        }

        Iterator<Integer> keyIterator = knownKeys.iterator();
        List<Integer> removeQueue = new ArrayList<>();
        int n = -1;

        for (DBEntry<IQuest> entry : QuestDatabase.INSTANCE.getEntries()) {
            while (n < entry.getID() && keyIterator.hasNext()) n = keyIterator.next();
            if (n != entry.getID()) removeQueue.add(entry.getID());
        }

        int removed = removeQueue.size();
        int[] bulkIDs = new int[removeQueue.size()];
        for (n = 0; n < bulkIDs.length; n++) bulkIDs[n] = removeQueue.get(n);
        NetQuestEdit.deleteQuests(bulkIDs);

        sender.sendMessage(new TextComponentTranslation("betterquesting.cmd.purge_hidden", removed));
    }

    @Override
    public String getPermissionNode() {
        return "betterquesting.command.admin.purge";
    }

    @Override
    public DefaultPermissionLevel getPermissionLevel() {
        return DefaultPermissionLevel.OP;
    }

    @Override
    public String getPermissionDescription() {
        return "Permission to purge all hidden quests and progression data however it does not delete any in new world defaults";
    }
}
