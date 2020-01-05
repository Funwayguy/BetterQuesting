package betterquesting.commands.admin;

import betterquesting.api.properties.NativeProps;
import betterquesting.commands.args.BooleanArgument;
import betterquesting.storage.QuestSettings;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.util.text.TranslationTextComponent;

public class QuestCommandEdit
{
    private static final String permNode = "betterquesting.command.admin.edit";
    
    public static ArgumentBuilder<CommandSource, ?> register()
    {
        return Commands.literal("edit").executes(QuestCommandEdit::runCommand).then(Commands.argument("state", BooleanArgument.INSTANCE)).executes((source) -> runCommand(source, BooleanArgument.getValue(source, "state")));
    }
    
    private static int runCommand(CommandContext<CommandSource> source)
    {
		return runCommand(source, !QuestSettings.INSTANCE.getProperty(NativeProps.EDIT_MODE));
    }
    
    private static int runCommand(CommandContext<CommandSource> source, boolean state)
    {
		QuestSettings.INSTANCE.setProperty(NativeProps.EDIT_MODE, state);
		source.getSource().sendFeedback(new TranslationTextComponent("betterquesting.cmd.edit", new TranslationTextComponent(state ? "options.on" : "options.off")), true);
        return 1;
    }
}
