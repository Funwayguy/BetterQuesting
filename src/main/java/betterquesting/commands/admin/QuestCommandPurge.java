package betterquesting.commands.admin;

import betterquesting.api.questing.IQuest;
import betterquesting.api.questing.IQuestLine;
import betterquesting.api.questing.IQuestLineEntry;
import betterquesting.api2.storage.DBEntry;
import betterquesting.commands.QuestCommandBase;
import betterquesting.handlers.SaveLoadHandler;
import betterquesting.network.PacketSender;
import betterquesting.questing.QuestDatabase;
import betterquesting.questing.QuestLineDatabase;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.server.permission.DefaultPermissionLevel;

import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.TreeSet;

public class QuestCommandPurge extends QuestCommandBase
{
    @Override
    public String getCommand()
    {
        return "purge_hidden_quests";
    }
    
    @Override
    public void runCommand(MinecraftServer server, CommandBase command, ICommandSender sender, String[] args)
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
        ArrayDeque<Integer> removeQueue = new ArrayDeque<>();
        int n = -1;
        
        for(DBEntry<IQuest> entry : QuestDatabase.INSTANCE.getEntries())
        {
            while(n < entry.getID() && keyIterator.hasNext()) n = keyIterator.next();
            if(n != entry.getID()) removeQueue.add(entry.getID());
        }
        
        int removed = removeQueue.size();
        
        while(removeQueue.size() > 0) QuestDatabase.INSTANCE.removeID(removeQueue.pop());
        
        sender.sendMessage(new TextComponentTranslation("betterquesting.cmd.purge_hidden", removed));
        PacketSender.INSTANCE.sendToAll(QuestDatabase.INSTANCE.getSyncPacket());
        SaveLoadHandler.INSTANCE.markDirty();
    }
	
	@Override
	public String getPermissionNode()
	{
		return "betterquesting.command.admin.purge";
	}

	@Override
	public DefaultPermissionLevel getPermissionLevel()
	{
		return DefaultPermissionLevel.OP;
	}

	@Override
	public String getPermissionDescription()
	{
		return "Permission to purge all hidden quests and progression data however it does not delete any in new world defaults";
	}
}
