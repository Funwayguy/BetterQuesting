package bq_standard.importers.hqm;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import net.minecraft.init.Items;
import org.apache.logging.log4j.Level;
import betterquesting.client.gui.GuiQuesting;
import betterquesting.client.gui.misc.GuiEmbedded;
import betterquesting.importers.ImporterBase;
import betterquesting.quests.QuestDatabase;
import betterquesting.quests.QuestInstance;
import betterquesting.quests.QuestLine;
import betterquesting.quests.rewards.RewardBase;
import betterquesting.quests.tasks.TaskBase;
import betterquesting.utils.BigItemStack;
import betterquesting.utils.JsonHelper;
import bq_standard.client.gui.importers.GuiHQMQuestImporter;
import bq_standard.core.BQ_Standard;
import bq_standard.importers.hqm.converters.rewards.HQMReward;
import bq_standard.importers.hqm.converters.rewards.HQMRewardChoice;
import bq_standard.importers.hqm.converters.rewards.HQMRewardReputation;
import bq_standard.importers.hqm.converters.rewards.HQMRewardStandard;
import bq_standard.importers.hqm.converters.tasks.HQMTask;
import bq_standard.importers.hqm.converters.tasks.HQMTaskCraft;
import bq_standard.importers.hqm.converters.tasks.HQMTaskDetect;
import bq_standard.importers.hqm.converters.tasks.HQMTaskKill;
import bq_standard.importers.hqm.converters.tasks.HQMTaskLocation;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class HQMQuestImporter extends ImporterBase
{
	// Commence project middle finger!
	
	public static HashMap<String, HQMTask> taskConverters = new HashMap<String, HQMTask>();
	public static HashMap<String, HQMReward> rewardConverters = new HashMap<String, HQMReward>();
	
	@SideOnly(Side.CLIENT)
	public static void StartImport()
	{
		idMap.clear(); // Reset ID map in preparation
		JFileChooser fc = new JFileChooser();
		fc.setDialogTitle("Import HQM Quest Line");
		fc.setCurrentDirectory(new File("."));
		fc.setMultiSelectionEnabled(true);
		FileNameExtensionFilter filter = new FileNameExtensionFilter("JSON Quest Line", "json");
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
				
				JsonObject json;
				
				try
				{
					FileReader fr = new FileReader(selected);
					Gson g = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
					json = g.fromJson(fr, JsonObject.class);
					fr.close();
				} catch(Exception e)
				{
					BQ_Standard.logger.log(Level.ERROR, "An error occured during import", e);
					continue;
				}
				
				if(json != null)
				{
					ImportQuestLine(json);
				}
			}
		}
	}
	
	public static HashMap<Integer, String> reputations = new HashMap<Integer, String>();
	
	public static void LoadReputations(JsonObject jsonRoot)
	{
		reputations.clear();
		
		int i = -1;
		
		for(JsonElement e : JsonHelper.GetArray(jsonRoot, "reputation"))
		{
			i++;
			
			if(e == null || !e.isJsonObject())
			{
				continue;
			}
			
			JsonObject jRep = e.getAsJsonObject();
			
			if(!jRep.has("name"))
			{
				continue;
			} else
			{
				reputations.put(i, JsonHelper.GetString(jRep, "name", "Reputation(" + i + ")"));
			}
		}
	}
	
	public static HashMap<String, QuestInstance> idMap = new HashMap<String, QuestInstance>(); // Use this to remap old IDs to new ones
	
	public static QuestInstance GetNewQuest(String oldID)
	{
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
		BQ_Standard.logger.log(Level.INFO, "Beginning import...");
		
		QuestLine questLine = new QuestLine();
		questLine.name = JsonHelper.GetString(json, "name", "HQM Quest Line");
		questLine.description = JsonHelper.GetString(json, "description", "No description");
		
		LoadReputations(json);
		
		JsonArray qlJson = JsonHelper.GetArray(json, "quests");
		
		ArrayList<String> loadedQuests = new ArrayList<String>(); // Just in case we have duplicate named quests
		
		for(int i = 0; i < qlJson.size(); i++)
		{
			JsonElement element = qlJson.get(i);
			
			if(element == null || !element.isJsonObject())
			{
				continue;
			}
			
			JsonObject jQuest = element.getAsJsonObject();
			
			String name = JsonHelper.GetString(jQuest, "name", "HQM Quest");
			String idName = name;
			
			if(loadedQuests.contains(idName))
			{
				int n = 1;
				while(loadedQuests.contains(idName + " (" + n + ")"))
				{
					n++;
				}
				BQ_Standard.logger.log(Level.WARN, "Found duplicate named quest " + name + ". Any quests with this pre-requisite will need repair!");
				idName = name + " (" + n + ")";
			}
			
			loadedQuests.add(idName);
			QuestInstance quest = GetNewQuest(idName);
			
			quest.name = name;
			quest.description = JsonHelper.GetString(jQuest, "description", "No Description");
			BigItemStack tmp = HQMUtilities.HQMStackT1(JsonHelper.GetObject(jQuest, "icon"));
			
			if(tmp != null)
			{
				quest.itemIcon = tmp;
			} else
			{
				quest.itemIcon = new BigItemStack(Items.nether_star);
			}
			
			for(JsonElement er : JsonHelper.GetArray(jQuest, "prerequisites"))
			{
				if(er == null || !er.isJsonPrimitive() || !er.getAsJsonPrimitive().isString())
				{
					continue;
				}
				
				String id = er.getAsJsonPrimitive().getAsString();
				
				if(id.startsWith("{") && id.contains("["))
				{
					String[] nParts = id.split("\\[");
					
					if(nParts.length > 1)
					{
						id = nParts[1].replaceFirst("]", "");
					}
				}
				
				QuestInstance preReq = GetNewQuest(id);
				preReq.name = id;
				quest.preRequisites.add(preReq);
			}
			
			for(JsonElement jt : JsonHelper.GetArray(jQuest, "tasks"))
			{
				if(jt == null || !jt.isJsonObject())
				{
					continue;
				}
				
				JsonObject jTask = jt.getAsJsonObject();
				String tType = JsonHelper.GetString(jTask, "type", "");
				
				if(tType == null || tType.length() <= 0)
				{
					continue;
				} else if(!taskConverters.containsKey(tType))
				{
					BQ_Standard.logger.log(Level.WARN, "Unidentified HQM task '" + tType + "'! Please report this so that it can be supported in future builds");
					continue;
				}
				
				ArrayList<TaskBase> tsks = taskConverters.get(tType).Convert(jTask);
				
				if(tsks != null && tsks.size() > 0)
				{
					quest.tasks.addAll(tsks);
				}
			}
			
			for(Entry<String,HQMReward> entry : rewardConverters.entrySet())
			{
				if(!jQuest.has(entry.getKey()))
				{
					continue;
				}
				
				ArrayList<RewardBase> rews = entry.getValue().Convert(jQuest.get(entry.getKey()));
				
				if(rews != null && rews.size() > 0)
				{
					quest.rewards.addAll(rews);
				}
			}
			
			if(questLine.questList.contains(quest))
			{
				BQ_Standard.logger.log(Level.WARN, "Tried to add duplicate quest " + quest + " to quest line " + questLine.name);
			} else
			{
				questLine.questList.add(quest);
			}
		}
		
		questLine.BuildTree();
		QuestDatabase.questLines.add(questLine);
		QuestDatabase.UpdateClients();
	}
	
	static
	{
		taskConverters.put("DETECT", new HQMTaskDetect(false));
		taskConverters.put("CONSUME", new HQMTaskDetect(true));
		taskConverters.put("CONSUME_QDS", new HQMTaskDetect(true));
		taskConverters.put("KILL", new HQMTaskKill());
		taskConverters.put("LOCATION", new HQMTaskLocation());
		taskConverters.put("CRAFT", new HQMTaskCraft());
		
		rewardConverters.put("reward", new HQMRewardStandard());
		rewardConverters.put("rewardchoice", new HQMRewardChoice());
		rewardConverters.put("reputationrewards", new HQMRewardReputation());
	}

	@Override
	public String getUnlocalisedName()
	{
		return "bq_standard.importer.hqm_quest.name";
	}

	@Override
	public GuiEmbedded getGui(GuiQuesting screen, int posX, int posY, int sizeX, int sizeY)
	{
		return new GuiHQMQuestImporter(screen, posX, posY, sizeX, sizeY);
	}
}
