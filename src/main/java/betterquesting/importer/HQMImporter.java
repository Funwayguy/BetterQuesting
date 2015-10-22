package betterquesting.importer;

import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import org.apache.logging.log4j.Level;
import betterquesting.core.BetterQuesting;
import betterquesting.quests.QuestDatabase;
import betterquesting.quests.QuestInstance;
import betterquesting.quests.QuestLine;
import betterquesting.utils.JsonHelper;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class HQMImporter
{
	// Commence project middle finger!
	
	@SideOnly(Side.CLIENT)
	public static void StartImport()
	{
		idMap.clear(); // Reset ID map in preparation
		JFileChooser fc = new JFileChooser();
		fc.setDialogTitle("Import HQM Quest Line");
		fc.setCurrentDirectory(new File("."));
		FileNameExtensionFilter filter = new FileNameExtensionFilter("JSON Quest Line", "json");
		fc.setFileFilter(filter);
		
		if(fc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION)
		{
			File selected = fc.getSelectedFile();
			
			if(selected != null && selected.exists())
			{
				JsonObject json;
				
				try
				{
					FileReader fr = new FileReader(selected);
					json = new Gson().fromJson(fr, JsonObject.class);
					fr.close();
				} catch(Exception e)
				{
					BetterQuesting.logger.log(Level.ERROR, "An error occured during import", e);
					return;
				}
				
				if(json != null)
				{
					ImportQuestLine(json);
				}
			}
		}
	}
	
	public static HashMap<Integer, QuestInstance> idMap = new HashMap<Integer, QuestInstance>(); // Use this to remap old IDs to new ones
	
	public static QuestInstance GetNewQuest(int oldID)
	{
		((BlockTNT)Blocks.tnt).func_
		
		if(idMap.containsKey(oldID))
		{
			return idMap.get(oldID);
		} else
		{
			QuestInstance quest = new QuestInstance(QuestDatabase.getUniqueID(), true);
			idMap.put(oldID, quest);
			return quest;
		}
	}
	
	public static void ImportQuestLine(JsonObject json)
	{
		BetterQuesting.logger.log(Level.INFO, "Beginning import...");
		
		QuestLine questLine = new QuestLine();
		questLine.name = JsonHelper.GetString(json, "name", "HQM Quest Line");
		
		JsonArray qlJson = JsonHelper.GetArray(json, "quests");
		
		for(int i = 0; i < qlJson.size(); i++)
		{
			JsonElement element = qlJson.get(i);
			
			if(element == null || !element.isJsonObject())
			{
				continue;
			}
			
			JsonObject jQuest = element.getAsJsonObject();
			
			QuestInstance quest = GetNewQuest(i);
			
			quest.name = JsonHelper.GetString(jQuest, "name", "HQM Quest");
			quest.description = JsonHelper.GetString(jQuest, "description", "No Description");
			
			for(JsonElement er : JsonHelper.GetArray(jQuest, "requirements"))
			{
				if(er == null || !er.isJsonPrimitive() || !er.getAsJsonPrimitive().isNumber())
				{
					continue;
				}
				
				int id = er.getAsJsonPrimitive().getAsInt();
				
				QuestInstance preReq = GetNewQuest(id);
				
				if(preReq == null)
				{
					BetterQuesting.logger.log(Level.ERROR, "Unable to create a new quest for ID " + id);
					continue;
				}
				
				quest.preRequisites.add(preReq);
			}
			
			questLine.questList.add(quest);
		}
		
		questLine.BuildTree();
		QuestDatabase.questLines.add(questLine);
		QuestDatabase.UpdateClients();
	}
	
	public static ItemStack HQMStack(JsonObject json)
	{
		Item item = (Item)Item.itemRegistry.getObject(JsonHelper.GetString(json, "id", "minecraft:stone"));
		
		if(item == null)
		{
			return new ItemStack(Blocks.stone);
		}
		
		ItemStack stack = new ItemStack(item, JsonHelper.GetNumber(json, "amount", 1).intValue(), JsonHelper.GetNumber(json, "damage", 1).intValue());
		
		if(json.has("nbt"))
		{
			try
			{
				NBTBase nbt = JsonToNBT.func_150315_a(JsonHelper.GetString(json, "nbt", ""));
				
				if(nbt != null && nbt instanceof NBTTagCompound)
				{
					stack.setTagCompound((NBTTagCompound)nbt);
				}
			} catch(Exception e)
			{
				BetterQuesting.logger.log(Level.ERROR, "Unable to convert HQM NBT data", e);
			}
		}
		
		return stack;
	}
}
