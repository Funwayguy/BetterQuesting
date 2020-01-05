package betterquesting.commands.admin;

import betterquesting.api.properties.NativeProps;
import betterquesting.api.questing.IQuest;
import betterquesting.handlers.SaveLoadHandler;
import betterquesting.network.handlers.NetChapterSync;
import betterquesting.network.handlers.NetQuestEdit;
import betterquesting.network.handlers.NetQuestSync;
import betterquesting.questing.QuestDatabase;
import betterquesting.questing.QuestLineDatabase;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.util.text.TranslationTextComponent;

public class QuestCommandDelete
{
    private static final String permNode = "betterquesting.command.admin.delete";
    
    public static ArgumentBuilder<CommandSource, ?> register()
    {
        LiteralArgumentBuilder<CommandSource> baseNode = Commands.literal("delete");
        
        baseNode.then(Commands.literal("all")).executes(QuestCommandDelete::deleteAll);
        
        baseNode.then(Commands.argument("quest_id", IntegerArgumentType.integer(0))).executes((context) -> deleteQuest(context, IntegerArgumentType.getInteger(context, "quest_id")));
        
        return baseNode;
    }
    
    private static int deleteQuest(CommandContext<CommandSource> context, int id)
    {
        IQuest quest = QuestDatabase.INSTANCE.getValue(id);
        if(quest == null) return 0;
        QuestLineDatabase.INSTANCE.removeQuest(id);
        
        NetQuestEdit.deleteQuests(new int[]{id});
        
        context.getSource().sendFeedback(new TranslationTextComponent("betterquesting.cmd.delete.single", new TranslationTextComponent(quest.getProperty(NativeProps.NAME))), true);
        return 1;
    }
    
    private static int deleteAll(CommandContext<CommandSource> context)
    {
        QuestDatabase.INSTANCE.reset();
        QuestLineDatabase.INSTANCE.reset();
        NetQuestSync.sendSync(null, null, true, true);
        NetChapterSync.sendSync(null, null);
        SaveLoadHandler.INSTANCE.markDirty();
        
        context.getSource().sendFeedback(new TranslationTextComponent("betterquesting.cmd.delete.all"), true);
        return 1;
    }
}
