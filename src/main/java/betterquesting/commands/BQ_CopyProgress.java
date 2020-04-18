package betterquesting.commands;

import betterquesting.api.questing.IQuest;
import betterquesting.api2.storage.DBEntry;
import betterquesting.network.handlers.NetQuestSync;
import betterquesting.questing.QuestDatabase;
import com.google.common.collect.Lists;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BQ_CopyProgress extends CommandBase {
    @Override
    public int getRequiredPermissionLevel() {
        return 2;
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<String> addTabCompletionOptions(ICommandSender p_71516_1_, String[] p_71516_2_) {
        if (p_71516_2_.length > 2)
            return null;

        String s = p_71516_2_.length != 0 ? p_71516_2_[p_71516_2_.length - 1] : "";
        return ((Stream<EntityPlayerMP>)MinecraftServer.getServer().getConfigurationManager().playerEntityList.stream().filter(i -> i instanceof EntityPlayerMP))
                .filter(o -> o.getDisplayName().startsWith(s))
                .map(EntityPlayer::getDisplayName).collect(Collectors.toList());
    }

    @Override
    public String getCommandName() {
        return "bq_copyquests";
    }

    @Override
    public List getCommandAliases() {
        return Lists.newArrayList(getCommandName());
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/bq_copyquests <toPlayer> [fromPlayer]";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        if(sender instanceof EntityPlayer) {
            if(args.length == 0 || args.length > 2) throw new CommandException("Wrong arguments");
            UUID ownUUID = (args.length == 2 ? getPlayer(sender, args[1]) : ((EntityPlayer) sender)).getPersistentID();
            EntityPlayerMP addPlayer = getPlayer(sender, args[0]);
            UUID addUUID = addPlayer.getPersistentID();
            long current = System.currentTimeMillis();
            List<Integer> ids = new ArrayList<>();
            for(DBEntry<IQuest> questDBEntry : QuestDatabase.INSTANCE.getEntries()) {
                IQuest quest = questDBEntry.getValue();
                if(quest.isComplete(ownUUID) && !quest.isComplete(addUUID)) {
                    quest.setComplete(addUUID, current);
                    ids.add(questDBEntry.getID());
                }
            }

            NetQuestSync.sendSync(addPlayer, ids.stream().mapToInt(i -> i).toArray(), false, true);
            sender.addChatMessage(new ChatComponentText("Completed " + ids.size() + " for " + addUUID));
        }
    }
}
