package betterquesting.importers.hqm;

import betterquesting.api.client.importers.IImporter;
import betterquesting.api.questing.IQuestDatabase;
import betterquesting.api.questing.IQuestLineDatabase;
import betterquesting.api.utils.FileExtensionFilter;
import betterquesting.api.utils.JsonHelper;
import betterquesting.network.handlers.NetLootImport;
import betterquesting.questing.rewards.loot.LootGroup;
import betterquesting.questing.rewards.loot.LootGroup.LootEntry;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class HQMBagImporter implements IImporter
{
	public static final HQMBagImporter INSTANCE = new HQMBagImporter();
	
	private List<LootGroup> hqmLoot = new ArrayList<>();
	
	@Override
	public String getUnlocalisedName()
	{
		return "bq_standard.importer.hqm_bag.name";
	}
	
	@Override
	public String getUnlocalisedDescription()
	{
		return "bq_standard.importer.hqm_bag.desc";
	}
	
	@Override
	public FileFilter getFileFilter()
	{
		return new FileExtensionFilter(".json");
	}
	
	private void ImportJsonBags(JsonArray json)
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
				int tmp = 0;
				
				JsonArray jWht = JsonHelper.GetArray(jGrp, "weights");
				
				for(int i = 0; i < jWht.size(); i++)
				{
					JsonElement w = jWht.get(i);
					
					if(w == null || !w.isJsonPrimitive() || !w.getAsJsonPrimitive().isNumber())
					{
						continue;
					}
					
					tmp += w.getAsInt() * (jWht.size() - i);
				}
				
				group.weight = Math.max(1, tmp/4);
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
				group.add(group.nextID(), lEntry);
			}
			
			hqmLoot.add(group);
		}
	}

	@Override
	public void loadFiles(IQuestDatabase questDB, IQuestLineDatabase lineDB, File[] files)
	{
		hqmLoot.clear();
		
		for(File selected : files)
		{
			if(selected == null || !selected.exists())
			{
				continue;
			}
			
			JsonArray json = ReadFromFile(selected);
			
			if(json != null && json.size() > 0)
			{
				ImportJsonBags(json);
			}
		}
		
		NBTTagCompound tags = new NBTTagCompound();
		NBTTagCompound base = new NBTTagCompound();
		NBTTagList jAry = new NBTTagList();
		
		for(LootGroup group : hqmLoot)
		{
			NBTTagCompound jGrp = new NBTTagCompound();
			group.writeToNBT(jGrp);
			jAry.appendTag(jGrp);
		}
  
		base.setTag("groups", jAry);
		tags.setTag("data", base);
        NetLootImport.importLoot(tags);
	}
	
	private JsonArray ReadFromFile(File file)
	{
		if(file == null || !file.exists())
		{
			return new JsonArray();
		}
		
		try
		{
			InputStreamReader fr = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8);
			JsonArray json = new Gson().fromJson(fr, JsonArray.class);
			fr.close();
			return json;
		} catch(Exception e)
		{
			return new JsonArray(); // Just a safety measure against NPEs
		}
	}
}
