package betterquesting.commands;

import betterquesting.commands.admin.*;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraftforge.server.permission.DefaultPermissionLevel;
import net.minecraftforge.server.permission.PermissionAPI;

import java.util.ArrayList;
import java.util.List;

public class BQ_CommandAdmin
{
    public static void register(final CommandDispatcher<CommandSource> dispatch)
    {
        // Could we string this into one big chain like vanilla does? Yes.
        // Are we going to do it that way? Hell no!
        // Why not? It's unreadable as fuck!
        
        LiteralArgumentBuilder<CommandSource> builder = Commands.literal("bq_admin");
        
        builder.requires((source) -> source.hasPermissionLevel(2)); // TODO: PermissionAPI support here?
        
        builder.then(QuestCommandEdit.register());
        builder.then(QuestCommandHardcore.register());
        builder.then(QuestCommandReset.register());
        builder.then(QuestCommandComplete.register());
        builder.then(QuestCommandDelete.register());
        builder.then(QuestCommandDefaults.register());
        builder.then(QuestCommandLives.register());
        builder.then(QuestCommandPurge.register());
        
        dispatch.register(builder);
    }
}
