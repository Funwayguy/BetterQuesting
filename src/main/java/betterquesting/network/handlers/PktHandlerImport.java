package betterquesting.network.handlers;

import betterquesting.api.api.QuestingAPI;
import betterquesting.api.network.IPacketHandler;
import betterquesting.api.questing.IQuest;
import betterquesting.api.questing.IQuestLine;
import betterquesting.api.questing.IQuestLineDatabase;
import betterquesting.api.questing.IQuestLineEntry;
import betterquesting.api2.storage.DBEntry;
import betterquesting.client.importers.ImportedQuestLines;
import betterquesting.client.importers.ImportedQuests;
import betterquesting.core.BetterQuesting;
import betterquesting.network.PacketSender;
import betterquesting.network.PacketTypeNative;
import betterquesting.questing.QuestDatabase;
import betterquesting.questing.QuestLineDatabase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import org.apache.logging.log4j.Level;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class PktHandlerImport implements IPacketHandler
{
	@Override
	public ResourceLocation getRegistryName()
	{
		return PacketTypeNative.IMPORT.GetLocation();
	}
	
	@Override
	public void handleServer(NBTTagCompound tag, EntityPlayerMP sender)
	{
		if(sender == null || sender.getServer() == null)
		{
			return;
		}
		
		boolean isOP = sender.getServer().getPlayerList().canSendCommands(sender.getGameProfile());
		
		if(!isOP)
		{
			BetterQuesting.logger.log(Level.WARN, "Player " + sender.getName() + " (UUID:" + QuestingAPI.getQuestingUUID(sender) + ") tried to import quests without OP permissions!");
			sender.sendStatusMessage(new TextComponentString(TextFormatting.RED + "You need to be OP to edit quests!"), false);
			return; // Player is not operator. Do nothing
		}
		
		NBTTagCompound jsonBase = tag.getCompoundTag("data");
		
		ImportedQuests impQuestDB = new ImportedQuests();
		IQuestLineDatabase impQuestLineDB = new ImportedQuestLines();
		
		impQuestDB.readFromNBT(jsonBase.getTagList("quests", 10), false);
		impQuestLineDB.readFromNBT(jsonBase.getTagList("lines", 10), false);
		
		BetterQuesting.logger.log(Level.INFO, "Importing " + impQuestDB.size() + " quest(s) and " + impQuestLineDB.size() + " quest line(s) from " + sender.getGameProfile().getName());
		
		HashMap<Integer,Integer> remapped = getRemappedIDs(impQuestDB.getEntries());
		
		for(DBEntry<IQuest> entry : impQuestDB.getEntries())
		{
		    int[] oldIDs = Arrays.copyOf(entry.getValue().getRequirements(), entry.getValue().getRequirements().length);
            
            for(int n = 0; n < oldIDs.length; n++)
            {
                if(remapped.containsKey(oldIDs[n]))
                {
                    oldIDs[n] = remapped.get(oldIDs[n]);
                }
            }
            
            entry.getValue().setRequirements(oldIDs);
            
			QuestDatabase.INSTANCE.add(remapped.get(entry.getID()), entry.getValue());
		}
		
		for(DBEntry<IQuestLine> questLine : impQuestLineDB.getEntries())
		{
		    List<DBEntry<IQuestLineEntry>> pendingQLE = new ArrayList<>();
		    
			for(DBEntry<IQuestLineEntry> qle : questLine.getValue().getEntries())
			{
			    pendingQLE.add(qle);
				questLine.getValue().removeID(qle.getID());
			}
			
			for(DBEntry<IQuestLineEntry> qle : pendingQLE)
            {
                if(!remapped.containsKey(qle.getID()))
                {
                    BetterQuesting.logger.error("Failed to import quest into quest line. Unable to remap ID " + qle.getID());
                    continue;
                }
                
                questLine.getValue().add(remapped.get(qle.getID()), qle.getValue());
            }
			
			QuestLineDatabase.INSTANCE.add(QuestLineDatabase.INSTANCE.nextID(), questLine.getValue());
		}
		
		PktHandlerQuestDB.INSTANCE.resyncAll(true);
		PacketSender.INSTANCE.sendToAll(PktHandlerLineDB.INSTANCE.getSyncPacket(null));
	}
	
	@Override
	public void handleClient(NBTTagCompound tag)
	{
	}
	
	/**
	 * Takes a list of imported IDs and returns a remapping to unused IDs
	 */
	private HashMap<Integer,Integer> getRemappedIDs(List<DBEntry<IQuest>> idList)
	{
	    int[] nextIDs = getNextIDs(idList.size());
		HashMap<Integer,Integer> remapped = new HashMap<>();
	    
	    for(int i = 0; i < nextIDs.length; i++)
        {
            remapped.put(idList.get(i).getID(), nextIDs[i]);
        }
		
		return remapped;
	}
	
	private int[] getNextIDs(int num)
    {
        List<DBEntry<IQuest>> listDB = QuestDatabase.INSTANCE.getEntries();
        int[] nxtIDs = new int[num];
        
        if(listDB.size() <= 0 || listDB.get(listDB.size() - 1).getID() == listDB.size() - 1)
        {
            for(int i = 0; i < num; i++) nxtIDs[i] = listDB.size() + i;
            return nxtIDs;
        }
        
        int n1 = 0;
        int n2 = 0;
        for(int i = 0; i < num; i++)
        {
            while(n2 < listDB.size() && listDB.get(n2).getID() == n1)
            {
                n1++;
                n2++;
            }
            
            nxtIDs[i] = n1++;
        }
        
        return nxtIDs;
    }
}
