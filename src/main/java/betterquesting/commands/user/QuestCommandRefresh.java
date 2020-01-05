package betterquesting.commands.user;

import betterquesting.api.api.QuestingAPI;
import betterquesting.api.questing.party.IParty;
import betterquesting.api2.storage.DBEntry;
import betterquesting.network.handlers.NetBulkSync;
import betterquesting.network.handlers.NetNameSync;
import betterquesting.questing.party.PartyManager;
import betterquesting.storage.NameCache;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TranslationTextComponent;

public class QuestCommandRefresh
{
    private static final String permNode = "betterquesting.command.user.refresh";
    
    public static ArgumentBuilder<CommandSource, ?> register()
    {
        return Commands.literal("refresh").executes(QuestCommandRefresh::runCommand);
    }
    
    private static int runCommand(CommandContext<CommandSource> context) throws CommandSyntaxException
    {
        ServerPlayerEntity player = context.getSource().asPlayer();
        MinecraftServer server = context.getSource().getServer();
	    
		if(server.isDedicatedServer() || !server.getServerOwner().equals(player.getGameProfile().getName()))
		{
            NetBulkSync.sendReset(player, true, true);
			context.getSource().sendFeedback(new TranslationTextComponent("betterquesting.cmd.refresh"), true);
		} else
        {
            boolean nameChanged = NameCache.INSTANCE.updateName(player);
            DBEntry<IParty> party = PartyManager.INSTANCE.getParty(QuestingAPI.getQuestingUUID(player));
            if(nameChanged && party != null) NetNameSync.quickSync(null, party.getID());
        }
		return 1;
    }
}
