package adv_director.network.handlers;

import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import org.apache.logging.log4j.Level;
import adv_director.api.api.QuestingAPI;
import adv_director.api.enums.EnumSaveType;
import adv_director.api.network.IPacketHandler;
import adv_director.api.questing.IQuestDatabase;
import adv_director.api.questing.IQuestLine;
import adv_director.api.questing.IQuestLineDatabase;
import adv_director.api.questing.IQuestLineEntry;
import adv_director.api.utils.JsonHelper;
import adv_director.api.utils.NBTConverter;
import adv_director.client.importers.ImportedQuestLines;
import adv_director.client.importers.ImportedQuests;
import adv_director.core.AdvDirector;
import adv_director.network.PacketSender;
import adv_director.network.PacketTypeNative;
import adv_director.questing.QuestDatabase;
import adv_director.questing.QuestLineDatabase;
import com.google.gson.JsonObject;

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
		if(sender == null)
		{
			return;
		}
		
		boolean isOP = sender.worldObj.getMinecraftServer().getPlayerList().canSendCommands(sender.getGameProfile());
		
		if(!isOP)
		{
			AdvDirector.logger.log(Level.WARN, "Player " + sender.getName() + " (UUID:" + QuestingAPI.getQuestingUUID(sender) + ") tried to import quests without OP permissions!");
			sender.addChatComponentMessage(new TextComponentString(TextFormatting.RED + "You need to be OP to edit quests!"));
			return; // Player is not operator. Do nothing
		}
		
		JsonObject jsonBase = NBTConverter.NBTtoJSON_Compound(tag.getCompoundTag("data"), new JsonObject());
		
		IQuestDatabase impQuestDB = new ImportedQuests();
		IQuestLineDatabase impQuestLineDB = new ImportedQuestLines();
		
		impQuestDB.readFromJson(JsonHelper.GetArray(jsonBase, "quests"), EnumSaveType.CONFIG);
		impQuestLineDB.readFromJson(JsonHelper.GetArray(jsonBase, "lines"), EnumSaveType.CONFIG);
		
		AdvDirector.logger.log(Level.INFO, "Importing " + impQuestDB.size() + " quest(s) and " + impQuestLineDB.size() + " quest line(s) from " + sender.getGameProfile().getName());
		
		HashMap<Integer,Integer> remapped = getRemappedIDs(impQuestDB.getAllKeys());
		
		for(Entry<Integer,Integer> entry : remapped.entrySet())
		{
			QuestDatabase.INSTANCE.add(impQuestDB.getValue(entry.getKey()), entry.getValue());
		}
		
		for(IQuestLine questLine : impQuestLineDB.getAllValues())
		{
			for(IQuestLineEntry qle : questLine.getAllValues())
			{
				int oldID = questLine.getKey(qle);
				questLine.removeKey(oldID);
				questLine.add(qle, remapped.get(oldID));
			}
			
			QuestLineDatabase.INSTANCE.add(questLine, QuestLineDatabase.INSTANCE.nextKey());
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
	private HashMap<Integer,Integer> getRemappedIDs(List<Integer> idList)
	{
		List<Integer> existing = QuestDatabase.INSTANCE.getAllKeys();
		HashMap<Integer,Integer> remapped = new HashMap<Integer,Integer>();
		
		int n = 0;
		
		for(int id : idList)
		{
			while(existing.contains(n) || remapped.containsValue(n))
			{
				n++;
			}
			
			remapped.put(id, n);
		}
		
		return remapped;
	}
}
