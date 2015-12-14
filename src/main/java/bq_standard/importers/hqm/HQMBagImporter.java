package bq_standard.importers.hqm;

import java.io.File;
import java.io.FileReader;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.apache.logging.log4j.Level;
import betterquesting.client.gui.GuiQuesting;
import betterquesting.client.gui.misc.GuiEmbedded;
import betterquesting.importers.ImporterBase;
import betterquesting.utils.JsonHelper;
import bq_standard.client.gui.importers.GuiHQMBagImporter;
import bq_standard.core.BQ_Standard;
import bq_standard.rewards.loot.LootGroup;
import bq_standard.rewards.loot.LootGroup.LootEntry;
import bq_standard.rewards.loot.LootRegistry;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class HQMBagImporter extends ImporterBase
{
	@Override
	public String getUnlocalisedName()
	{
		return "bq_standard.importer.hqm_bag.name";
	}
	
	public static void StartImport()
	{
		JFileChooser fc = new JFileChooser();
		fc.setDialogTitle("Import HQM Reward Bags");
		fc.setCurrentDirectory(new File("."));
		fc.setMultiSelectionEnabled(true);
		FileNameExtensionFilter filter = new FileNameExtensionFilter("JSON Reward Bags", "json");
		fc.setFileFilter(filter);
		
		if(fc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION)
		{
			File[] selList = fc.getSelectedFiles();
			
			for(File selected : selList)
			{
				if(selected == null || !selected.exists())
				{
					continue;
				}
				
				JsonArray json; // Bag.json is formatting as an array!
				
				try
				{
					FileReader fr = new FileReader(selected);
					Gson g = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
					json = g.fromJson(fr, JsonArray.class);
					fr.close();
				} catch(Exception e)
				{
					BQ_Standard.logger.log(Level.ERROR, "An error occured during import", e);
					continue;
				}
				
				if(json != null)
				{
					ImportJsonBags(json);
				}
			}
		}
	}
	
	public static void ImportJsonBags(JsonArray json)
	{
		for(JsonElement e : json)
		{
			if(e == null || !e.isJsonObject())
			{
				continue;
			}
			
			JsonObject jGrp = e.getAsJsonObject();
			
			LootGroup group = new LootGroup();
			group.name = JsonHelper.GetString(jGrp, "name", "HQM Loot");
			try
			{
				group.weight = JsonHelper.GetArray(jGrp, "weights").get(4).getAsInt();
			} catch(Exception ex)
			{
				group.weight = 1;
			}
			
			for(JsonElement e2 : JsonHelper.GetArray(jGrp, "groups"))
			{
				if(e2 == null || !e2.isJsonObject())
				{
					continue;
				}
				
				JsonObject je = e2.getAsJsonObject();
				LootEntry lEntry = new LootEntry();
				lEntry.weight = JsonHelper.GetNumber(je, "limit", 1).intValue();
				for(JsonElement ji : JsonHelper.GetArray(je, "items"))
				{
					if(ji == null || !ji.isJsonObject())
					{
						continue;
					}
					
					lEntry.items.add(HQMUtilities.HQMStackT1(ji.getAsJsonObject()));
				}
				group.lootEntry.add(lEntry);
			}
			
			LootRegistry.registerGroup(group);
		}
	}
	
	@Override
	public GuiEmbedded getGui(GuiQuesting screen, int posX, int posY, int sizeX, int sizeY)
	{
		return new GuiHQMBagImporter(screen, posX, posY, sizeX, sizeY);
	}
}
