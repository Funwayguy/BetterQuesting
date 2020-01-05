package betterquesting.commands.admin;

import betterquesting.api.api.QuestingAPI;
import betterquesting.api.properties.NativeProps;
import betterquesting.api.questing.IQuest;
import betterquesting.network.handlers.NetQuestEdit;
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
import java.util.UUID;

public class QuestCommandComplete
{
    private static final String permNode = "betterquesting.command.admin.complete";
    
    public static ArgumentBuilder<CommandSource, ?> register()
    {
        LiteralArgumentBuilder<CommandSource> baseNode = Commands.literal("complete");
        
        baseNode.then(Commands.literal("all")).then(Commands.argument("target", EntityArgument.players()))
                .executes((context) -> completeAll(context, EntityArgument.getPlayers(context, "target")));
        
        baseNode.then(Commands.argument("quest_id", IntegerArgumentType.integer(0))).then(Commands.argument("target", EntityArgument.players()))
                .executes((context) -> completeQuest(context, IntegerArgumentType.getInteger(context, "quest_id"), EntityArgument.getPlayers(context, "target")));
        
        return baseNode;
    }
    
    private static int completeQuest(CommandContext<CommandSource> context, int id, Collection<ServerPlayerEntity> targets)
    {
        IQuest quest = QuestDatabase.INSTANCE.getValue(id);
        if(quest == null) return 0;
        
        int[] idAry = new int[]{id};
        String qName = quest.getProperty(NativeProps.NAME);
        
        for(ServerPlayerEntity player : targets)
        {
            UUID playerID = QuestingAPI.getQuestingUUID(player);
            NetQuestEdit.setQuestStates(idAry, true, playerID);
            context.getSource().sendFeedback(new TranslationTextComponent("betterquesting.cmd.complete", qName, player.getGameProfile().getName()), true);
        }
        
        return 1;
    }
    
    private static int completeAll(CommandContext<CommandSource> context, Collection<ServerPlayerEntity> targets)
    {
        for(ServerPlayerEntity player : targets)
        {
            UUID playerID = QuestingAPI.getQuestingUUID(player);
            NetQuestEdit.setQuestStates(null, true, playerID);
            context.getSource().sendFeedback(new TranslationTextComponent("betterquesting.cmd.complete", "ALL", player.getGameProfile().getName()), true);
        }
        
        return 1;
    }
}
