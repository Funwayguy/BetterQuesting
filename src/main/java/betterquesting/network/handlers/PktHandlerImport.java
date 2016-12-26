package betterquesting.network.handlers;

import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import org.apache.logging.log4j.Level;
import betterquesting.api.api.QuestingAPI;
import betterquesting.api.enums.EnumSaveType;
import betterquesting.api.network.IPacketHandler;
import betterquesting.api.questing.IQuestDatabase;
import betterquesting.api.questing.IQuestLine;
import betterquesting.api.questing.IQuestLineDatabase;
import betterquesting.api.questing.IQuestLineEntry;
import betterquesting.api.utils.JsonHelper;
import betterquesting.api.utils.NBTConverter;
import betterquesting.client.importers.ImportedQuestLines;
import betterquesting.client.importers.ImportedQuests;
import betterquesting.core.BetterQuesting;
import betterquesting.network.PacketTypeNative;
import betterquesting.questing.QuestDatabase;
import betterquesting.questing.QuestLineDatabase;
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
			BetterQuesting.logger.log(Level.WARN, "Player " + sender.getName() + " (UUID:" + QuestingAPI.getQuestingUUID(sender) + ") tried to import quests without OP permissions!");
			sender.addChatComponentMessage(new TextComponentString(TextFormatting.RED + "You need to be OP to edit quests!"));
			return; // Player is not operator. Do nothing
		}
		
		JsonObject jsonBase = NBTConverter.NBTtoJSON_Compound(tag.getCompoundTag("data"), new JsonObject());
		
		IQuestDatabase impQuestDB = new ImportedQuests();
		IQuestLineDatabase impQuestLineDB = new ImportedQuestLines();
		
		impQuestDB.readFromJson(JsonHelper.GetArray(jsonBase, "quests"), EnumSaveType.CONFIG);
		impQuestLineDB.readFromJson(JsonHelper.GetArray(jsonBase, "lines"), EnumSaveType.CONFIG);
		
		BetterQuesting.logger.log(Level.INFO, "Importing " + impQuestDB.size() + " quest(s) from " + sender.getGameProfile().getName());
		
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
			while(existing.contains(n))
			{
				n++;
			}
			
			remapped.put(id, n);
		}
		
		return remapped;
	}
}
