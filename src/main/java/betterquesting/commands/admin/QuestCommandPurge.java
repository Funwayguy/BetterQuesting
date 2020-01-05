package betterquesting.commands.admin;

import betterquesting.api.questing.IQuest;
import betterquesting.api.questing.IQuestLine;
import betterquesting.api.questing.IQuestLineEntry;
import betterquesting.api2.storage.DBEntry;
import betterquesting.network.handlers.NetQuestEdit;
import betterquesting.questing.QuestDatabase;
import betterquesting.questing.QuestLineDatabase;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

public class QuestCommandPurge
{
    private static final String permNode = "betterquesting.command.admin.purge";
    
    public static ArgumentBuilder<CommandSource, ?> register()
    {
        return Commands.literal("purge_hidden_quests").executes(QuestCommandPurge::runCommand);
    }
    
    private static int runCommand(CommandContext<CommandSource> context)
    {
        TreeSet<Integer> knownKeys = new TreeSet<>();
        
        for(DBEntry<IQuestLine> entry : QuestLineDatabase.INSTANCE.getEntries())
        {
            for(DBEntry<IQuestLineEntry> qle : entry.getValue().getEntries())
            {
                knownKeys.add(qle.getID());
            }
        }
        
        Iterator<Integer> keyIterator = knownKeys.iterator();
        List<Integer> removeQueue = new ArrayList<>();
        int n = -1;
        
        for(DBEntry<IQuest> entry : QuestDatabase.INSTANCE.getEntries())
        {
            while(n < entry.getID() && keyIterator.hasNext()) n = keyIterator.next();
            if(n != entry.getID()) removeQueue.add(entry.getID());
        }
        
        int removed = removeQueue.size();
        int[] bulkIDs = new int[removeQueue.size()];
        for(n = 0; n < bulkIDs.length; n++) bulkIDs[n] = removeQueue.get(n);
        NetQuestEdit.deleteQuests(bulkIDs);
        
        context.getSource().sendFeedback(new TranslationTextComponent("betterquesting.cmd.purge_hidden", removed), true);
        return 1;
    }
}
