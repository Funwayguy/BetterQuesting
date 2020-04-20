package betterquesting.commands.user;

import betterquesting.api.properties.NativeProps;
import betterquesting.commands.args.BooleanArgument;
import betterquesting.storage.QuestSettings;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.util.text.TranslationTextComponent;

public class QuestCommandSPHardcore
{
    private static final String permNode = "betterquesting.command.user.hardcore";
    
    public static ArgumentBuilder<CommandSource, ?> register()
    {
        return Commands.literal("hardcore").requires((source) -> {
            try
            {
                return source.getServer().isSinglePlayer() && source.getServer().isServerOwner(source.asPlayer().getGameProfile()); // isOwner
            } catch(CommandSyntaxException e)
            {
                return false;
            }
        }).executes(QuestCommandSPHardcore::runCommand).then(Commands.argument("state", BooleanArgument.INSTANCE)).executes((source) -> runCommand(source, BooleanArgument.getValue(source, "state")));
    }
    
    private static int runCommand(CommandContext<CommandSource> source)
    {
		return runCommand(source, !QuestSettings.INSTANCE.getProperty(NativeProps.HARDCORE));
    }
    
    private static int runCommand(CommandContext<CommandSource> source, boolean state)
    {
		QuestSettings.INSTANCE.setProperty(NativeProps.HARDCORE, state);
		source.getSource().sendFeedback(new TranslationTextComponent("betterquesting.cmd.hardcore", new TranslationTextComponent(state ? "options.on" : "options.off")), true);
        return 1;
    }
}
