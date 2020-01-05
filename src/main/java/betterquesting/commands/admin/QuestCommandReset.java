package betterquesting.commands.admin;

import betterquesting.api.api.QuestingAPI;
import betterquesting.api.properties.NativeProps;
import betterquesting.api.questing.IQuest;
import betterquesting.api2.storage.DBEntry;
import betterquesting.network.handlers.NetQuestSync;
import betterquesting.questing.QuestDatabase;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class QuestCommandReset
{
    private static final String permNode = "betterquesting.command.admin.reset";
    
    public static ArgumentBuilder<CommandSource, ?> register()
    {
        LiteralArgumentBuilder<CommandSource> baseNode = Commands.literal("reset");
        
        baseNode.then(Commands.literal("all")).then(Commands.argument("target", EntityArgument.players()))
                .executes((context) -> resetAll(context, EntityArgument.getPlayers(context, "target")));
        
        baseNode.then(Commands.argument("quest_id", IntegerArgumentType.integer(0))).then(Commands.argument("target", EntityArgument.players()))
                .executes((context) -> resetQuest(context, IntegerArgumentType.getInteger(context, "quest_id"), EntityArgument.getPlayers(context, "target")));
        
        return baseNode;
    }
    
    private static int resetQuest(CommandContext<CommandSource> context, int id, Collection<ServerPlayerEntity> targets)
    {
        IQuest quest = QuestDatabase.INSTANCE.getValue(id);
        if(quest == null) return 0;
        
        for(ServerPlayerEntity player : targets)
        {
            final UUID playerID = QuestingAPI.getQuestingUUID(player);
            quest.resetUser(playerID, true);
            context.getSource().sendFeedback(new TranslationTextComponent("betterquesting.cmd.reset.player_single", new TranslationTextComponent(quest.getProperty(NativeProps.NAME)), player.getGameProfile().getName()), true);
            NetQuestSync.sendSync(player, new int[]{id}, false, true);
        }
        
        return 1;
    }
    
    private static int resetAll(CommandContext<CommandSource> context, Collection<ServerPlayerEntity> targets)
    {
        List<DBEntry<IQuest>> dbList = QuestDatabase.INSTANCE.getEntries();
        
        for(ServerPlayerEntity player : targets)
        {
            final UUID playerID = QuestingAPI.getQuestingUUID(player);
            dbList.parallelStream().forEach((entry) -> entry.getValue().resetUser(playerID, true));
            context.getSource().sendFeedback(new TranslationTextComponent("betterquesting.cmd.reset.player_all", player.getGameProfile().getName()), true);
            NetQuestSync.sendSync(player, null, false, true);
        }
        
        return 1;
    }
}
