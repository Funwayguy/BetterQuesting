package betterquesting.commands.admin;

import betterquesting.api.api.QuestingAPI;
import betterquesting.api.properties.NativeProps;
import betterquesting.network.handlers.NetLifeSync;
import betterquesting.network.handlers.NetSettingSync;
import betterquesting.storage.LifeDatabase;
import betterquesting.storage.QuestSettings;
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

public class QuestCommandLives
{
    private static final String permNode = "betterquesting.command.admin.lives";
    
    public static ArgumentBuilder<CommandSource, ?> register()
    {
        LiteralArgumentBuilder<CommandSource> baseNode = Commands.literal("lives");
        
        baseNode.then(Commands.literal("add"))
                .then(Commands.argument("amount", IntegerArgumentType.integer()))
                .then(Commands.argument("target", EntityArgument.players()))
                .executes((context) -> addLives(context, IntegerArgumentType.getInteger(context, "amount"), EntityArgument.getPlayers(context, "target")));
        
        baseNode.then(Commands.literal("set"))
                .then(Commands.argument("amount", IntegerArgumentType.integer()))
                .then(Commands.argument("target", EntityArgument.players()))
                .executes((context) -> setLives(context, IntegerArgumentType.getInteger(context, "amount"), EntityArgument.getPlayers(context, "target")));
        
        baseNode.then(Commands.literal("max"))
                .then(Commands.argument("amount", IntegerArgumentType.integer()))
                .executes((context) -> maxLives(context, IntegerArgumentType.getInteger(context, "amount")));
        
        baseNode.then(Commands.literal("default"))
                .then(Commands.argument("amount", IntegerArgumentType.integer()))
                .executes((context) -> defLives(context, IntegerArgumentType.getInteger(context, "amount")));
        
        return baseNode;
    }
    
    private static int addLives(CommandContext<CommandSource> context, int amount, Collection<ServerPlayerEntity> targets)
    {
        for(ServerPlayerEntity player : targets)
        {
            UUID playerID = QuestingAPI.getQuestingUUID(player);
            int lives = LifeDatabase.INSTANCE.getLives(playerID) + amount;
            LifeDatabase.INSTANCE.setLives(playerID, lives);
            
            NetLifeSync.sendSync(new ServerPlayerEntity[]{player}, new UUID[]{playerID});
    
            if(amount >= 0)
            {
                context.getSource().sendFeedback(new TranslationTextComponent("betterquesting.cmd.lives.add_player", amount, player.getGameProfile().getName(), lives), true);
            } else
            {
                context.getSource().sendFeedback(new TranslationTextComponent("betterquesting.cmd.lives.remove_player", -amount, player.getGameProfile().getName(), lives), true);
            }
        }
        return targets.size();
    }
    
    private static int setLives(CommandContext<CommandSource> context, int amount, Collection<ServerPlayerEntity> targets)
    {
        for(ServerPlayerEntity player : targets)
        {
            UUID playerID = QuestingAPI.getQuestingUUID(player);
            int lives = Math.max(0, amount);
            LifeDatabase.INSTANCE.setLives(playerID, lives);
            
            NetLifeSync.sendSync(new ServerPlayerEntity[]{player}, new UUID[]{playerID});
            
            context.getSource().sendFeedback(new TranslationTextComponent("betterquesting.cmd.lives.set_player", player.getGameProfile().getName(), lives), true);
        }
        return targets.size();
    }
    
    private static int maxLives(CommandContext<CommandSource> context, int amount)
    {
        int lives = Math.max(1, amount);
        QuestSettings.INSTANCE.setProperty(NativeProps.LIVES_MAX, lives);
        context.getSource().sendFeedback(new TranslationTextComponent("betterquesting.cmd.lives.max", lives), true);
        NetSettingSync.sendSync(null);
        return 1;
    }
    
    private static int defLives(CommandContext<CommandSource> context, int amount)
    {
        int lives = Math.max(1, amount);
        QuestSettings.INSTANCE.setProperty(NativeProps.LIVES_DEF, lives);
        context.getSource().sendFeedback(new TranslationTextComponent("betterquesting.cmd.lives.default", lives), true);
        NetSettingSync.sendSync(null);
        return 1;
    }
}
