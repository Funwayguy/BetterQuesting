package betterquesting.commands;

import betterquesting.api.network.QuestingPacket;
import betterquesting.api.questing.IQuest;
import betterquesting.api2.storage.DBEntry;
import betterquesting.network.PacketSender;
import betterquesting.questing.QuestDatabase;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentString;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class BQ_CopyProgress extends CommandBase {

    private static final ResourceLocation ID_NAME = new ResourceLocation("betterquesting:quest_sync");

    private static final String COMMAND_NAME = "bq_copyquests";
    private static final String COMMAND_USAGE = "/bq_copyquests [toPlayer] <fromPlayer>";

    @Override
    public int getRequiredPermissionLevel() {
        return 2;
    }

    @Nonnull
    @Override
    public String getName() {
        return COMMAND_NAME;
    }

    @Nonnull
    @Override
    public String getUsage(@Nonnull ICommandSender sender) {
        return COMMAND_USAGE;
    }

    @Nonnull
    @Override
    public List<String> getAliases() {
        return Collections.singletonList(getName());
    }

    @Override
    public void execute(@Nonnull MinecraftServer server, @Nonnull ICommandSender sender, @Nonnull String[] args) throws CommandException {
        if (sender instanceof EntityPlayer) {
            if (args.length == 0 || args.length > 2) {
                throw new CommandException(COMMAND_USAGE);
            }

            UUID ownUUID = args.length == 2 ? getPlayer(server, sender, args[1]).getPersistentID() : ((EntityPlayer) sender).getPersistentID();
            EntityPlayerMP addPlayer = getPlayer(server, sender, args[0]);
            UUID addUUID = addPlayer.getPersistentID();

            long current = System.currentTimeMillis();
            int questsCompleted = 0;
            for (DBEntry<IQuest> questDBEntry : QuestDatabase.INSTANCE.getEntries()) {
                IQuest quest = questDBEntry.getValue();
                if (quest.isComplete(ownUUID) && !quest.isComplete(addUUID)) {
                    quest.setComplete(addUUID, current);
                    PacketSender.INSTANCE.sendToPlayers(new QuestingPacket(ID_NAME, quest.getCompletionInfo(addUUID)), addPlayer);

                    questsCompleted++;
                }
            }
            sender.sendMessage(new TextComponentString("Completed " + questsCompleted + " for " + addPlayer.getDisplayNameString()));
        }
    }
}
