package betterquesting.commands;

import betterquesting.api.questing.IQuest;
import betterquesting.api2.storage.DBEntry;
import betterquesting.network.handlers.NetQuestSync;
import betterquesting.questing.QuestDatabase;
import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerProfileCache;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.common.UsernameCache;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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
        return "/bq_copyquests <toPlayer> |OR| /bq_copyquests <fromPlayer> <toPlayer>";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        if(args.length == 0 || args.length > 2) {
            throw new CommandException("Wrong arguments");
        }

        UUID fromUUID;
        UUID toUUID;
        if (args.length == 2)
        {
            fromUUID = GetPlayerUUID(args[0]);
            toUUID = GetPlayerUUID(args[1]);
        }else if(sender instanceof EntityPlayer){
            fromUUID = ((EntityPlayer) sender).getPersistentID();
            toUUID = GetPlayerUUID(args[0]);
        }else {
            throw new CommandException("Wrong arguments");
        }

        long current = System.currentTimeMillis();
        List<Integer> ids = new ArrayList<>();
        for(DBEntry<IQuest> questDBEntry : QuestDatabase.INSTANCE.getEntries()) {
            IQuest quest = questDBEntry.getValue();
            if(quest.isComplete(fromUUID) && !quest.isComplete(toUUID)) {
                quest.setComplete(toUUID, current);
                ids.add(questDBEntry.getID());
            }
        }

        EntityPlayerMP player = getPlayerAdvanced(sender, toUUID.toString());
        if (player != null){
            NetQuestSync.sendSync(player, ids.stream().mapToInt(i -> i).toArray(), false, true);
        }

        sender.addChatMessage(new ChatComponentText("Completed " + ids.size() + " for " + toUUID));
    }

    @SuppressWarnings("unchecked")
    public static EntityPlayerMP getPlayerAdvanced(ICommandSender p_82359_0_, String p_82359_1_){
        Optional onlinePlayer = MinecraftServer.getServer().getConfigurationManager().playerEntityList.stream().filter(i -> i instanceof EntityPlayerMP)
                .filter(o -> ((EntityPlayerMP) o).getPersistentID().toString().equals(p_82359_1_) || ((EntityPlayerMP) o).getDisplayName().equals(p_82359_1_)).findFirst();
        try {
            return onlinePlayer.isPresent() ? (EntityPlayerMP) onlinePlayer.get() : getPlayer(p_82359_0_, p_82359_1_);
        }catch (Exception e){
            return null;
        }
    }


    @SuppressWarnings("unchecked")
    private static UUID GetPlayerUUID(String data){
        try {
            return UUID.fromString(data);
        } catch (IllegalArgumentException e) {
            Optional onlinePlayer = MinecraftServer.getServer().getConfigurationManager().playerEntityList.stream().filter(i -> i instanceof EntityPlayerMP)
                    .filter(o -> ((EntityPlayerMP) o).getPersistentID().toString().equals(data) || ((EntityPlayerMP) o).getDisplayName().equals(data)).findFirst();
            if (onlinePlayer.isPresent())
                return ((EntityPlayerMP)onlinePlayer.get()).getPersistentID();

            GameProfile gameProfile = new PlayerProfileCache(MinecraftServer.getServer(), MinecraftServer.field_152367_a).func_152655_a(data);
            if (gameProfile != null)
                return gameProfile.getId();

            return UUID.nameUUIDFromBytes(data.getBytes(StandardCharsets.UTF_8));
        }
    }
}
