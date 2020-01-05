package betterquesting.commands;

import betterquesting.commands.user.QuestCommandRefresh;
import betterquesting.commands.user.QuestCommandSPHardcore;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;

public class BQ_CommandUser
{
    public static void register(final CommandDispatcher<CommandSource> dispatch)
    {
        // Could we string this into one big chain like vanilla does? Yes.
        // Are we going to do it that way? Hell no!
        // Why not? It's unreadable as fuck!
        
        LiteralArgumentBuilder<CommandSource> builder = Commands.literal("bq_admin");
        
        builder.requires((source) -> source.hasPermissionLevel(0)); // TODO: PermissionAPI support here?
        
        builder.then(QuestCommandRefresh.register());
        builder.then(QuestCommandSPHardcore.register());
        
        dispatch.register(builder);
    }
}
