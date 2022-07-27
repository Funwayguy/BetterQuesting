package betterquesting.commands.user;

import betterquesting.api.api.QuestingAPI;
import betterquesting.api.questing.party.IParty;
import betterquesting.api2.storage.DBEntry;
import betterquesting.commands.QuestCommandBase;
import betterquesting.network.handlers.NetBulkSync;
import betterquesting.network.handlers.NetNameSync;
import betterquesting.questing.party.PartyManager;
import betterquesting.storage.NameCache;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentTranslation;

public class QuestCommandRefresh extends QuestCommandBase {
    @Override
    public String getCommand() {
        return "refresh";
    }

    @Override
    public void runCommand(MinecraftServer server, CommandBase command, ICommandSender sender, String[] args) {
        if (!(sender instanceof EntityPlayerMP)) return;
        EntityPlayerMP player = (EntityPlayerMP) sender;

        if (server.isDedicatedServer()
                || !server.getServerOwner().equals(player.getGameProfile().getName())) {
            NetBulkSync.sendReset(player, true, true);
            sender.addChatMessage(new ChatComponentTranslation("betterquesting.cmd.refresh"));
        } else {
            boolean nameChanged = NameCache.INSTANCE.updateName(player);
            DBEntry<IParty> party = PartyManager.INSTANCE.getParty(QuestingAPI.getQuestingUUID(player));
            if (nameChanged && party != null) NetNameSync.quickSync(null, party.getID());
        }
    }
}
