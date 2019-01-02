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
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

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
		
		for(Entry<Integer,Integer> entry : remapped.entrySet())
		{
			QuestDatabase.INSTANCE.add(entry.getValue(), impQuestDB.getValue(entry.getKey()));
		}
		
		for(DBEntry<IQuestLine> questLine : impQuestLineDB.getEntries())
		{
			for(DBEntry<IQuestLineEntry> qle : questLine.getValue().getEntries())
			{
				int oldID = qle.getID();
				questLine.getValue().removeID(qle.getID());
				questLine.getValue().add(remapped.get(oldID), qle.getValue());
			}
			
			QuestLineDatabase.INSTANCE.add(QuestLineDatabase.INSTANCE.nextID(), questLine.getValue());
		}
		
		PacketSender.INSTANCE.sendToAll(QuestDatabase.INSTANCE.getSyncPacket());
		PacketSender.INSTANCE.sendToAll(QuestLineDatabase.INSTANCE.getSyncPacket());
	}
	
	@Override
	public void handleClient(NBTTagCompound tag)
	{
	}
	
	/**
	 * Takes a list of imported IDs and returns a remapping to unused IDs
	 */
	private HashMap<Integer,Integer> getRemappedIDs(DBEntry<IQuest>[] idList)
	{
		List<Integer> existing = new ArrayList<>();
		
		for(DBEntry<IQuest> entry : QuestDatabase.INSTANCE.getEntries())
		{
			existing.add(entry.getID());
		}
		
		HashMap<Integer,Integer> remapped = new HashMap<>();
		
		int n = 0;
		
		for(DBEntry<IQuest> id : idList)
		{
			while(existing.contains(n) || remapped.containsValue(n))
			{
				n++;
			}
			
			remapped.put(id.getID(), n);
		}
		
		return remapped;
	}
}
