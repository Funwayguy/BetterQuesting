package betterquesting.importers.hqm;

import betterquesting.api.api.QuestingAPI;
import betterquesting.api.client.importers.IImporter;
import betterquesting.api.properties.NativeProps;
import betterquesting.api.questing.*;
import betterquesting.api.questing.rewards.IReward;
import betterquesting.api.questing.tasks.ITask;
import betterquesting.api.utils.BigItemStack;
import betterquesting.api.utils.FileExtensionFilter;
import betterquesting.api.utils.JsonHelper;
import betterquesting.api2.storage.IDatabaseNBT;
import betterquesting.api2.utils.BQThreadedIO;
import betterquesting.core.BetterQuesting;
import betterquesting.importers.hqm.converters.HQMRep;
import betterquesting.importers.hqm.converters.rewards.*;
import betterquesting.importers.hqm.converters.tasks.*;
import com.google.gson.*;
import net.minecraft.init.Items;
import net.minecraft.nbt.NBTTagList;
import org.apache.logging.log4j.Level;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.Future;
import java.util.function.Function;

public class HQMQuestImporter implements IImporter
{
	public static final HQMQuestImporter INSTANCE = new HQMQuestImporter();
	private static final FileFilter FILTER = new FileExtensionFilter(".json");
	
	private static HashMap<String, Function<JsonObject, ITask[]>> taskConverters = new HashMap<>();
	private static HashMap<String, Function<JsonElement, IReward[]>> rewardConverters = new HashMap<>();
	
	public HashMap<String, HQMRep> reputations = new HashMap<>();
	
	private HashMap<String, IQuest> idMap = new HashMap<>(); // Use this to remap old IDs to new ones
	
	@Override
	public FileFilter getFileFilter()
	{
		return FILTER;
	}

	@Override
	public String getUnlocalisedName()
	{
		return "bq_standard.importer.hqm_quest.name";
	}

	@Override
	public String getUnlocalisedDescription()
	{
		return "bq_standard.importer.hqm_quest.desc";
	}

	@Override
	public void loadFiles(IQuestDatabase questDB, IQuestLineDatabase lineDB, File[] files)
	{
		reputations.clear();
		idMap.clear();
		
		for(File selected : files) // Pre-search for reputations required for tasks
		{
			if(selected == null || !selected.exists() || !selected.getName().equalsIgnoreCase("reputations.json")) continue;
			
			JsonArray json = ReadArrayFromFile(selected);
			LoadReputations(json);
        }
		
		for(File selected : files)
		{
			if(selected == null || !selected.exists() || selected.getName().equalsIgnoreCase("reputations.json")) continue;
			
			JsonObject json = JsonHelper.ReadFromFile(selected);
			ImportQuestLine(questDB, lineDB, json);
		}
	}
	
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	
	private static JsonArray ReadArrayFromFile(File file)
	{
		Future<JsonArray> task = BQThreadedIO.INSTANCE.enqueue(() -> {
			if(file == null || !file.exists())
			{
				return new JsonArray();
			}
			
			try(InputStreamReader fr = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))
			{
				return GSON.fromJson(fr, JsonArray.class);
			} catch(Exception e)
			{
				QuestingAPI.getLogger().log(Level.ERROR, "An error occured while loading JSON from file:", e);
				
				int i = 0;
				File bkup = new File(file.getParent(), "malformed_" + file.getName() + i + ".json");
				
				while(bkup.exists())
				{
					i++;
					bkup = new File(file.getParent(), "malformed_" + file.getName() + i + ".json");
				}
				
				QuestingAPI.getLogger().log(Level.ERROR, "Creating backup at: " + bkup.getAbsolutePath());
				JsonHelper.CopyPaste(file, bkup);
				
				return new JsonArray(); // Just a safety measure against NPEs
			}
		});
		
		try
		{
			return task.get(); // Wait for other scheduled file ops to finish
		} catch(Exception e)
		{
		    QuestingAPI.getLogger().error("Unable to read from file " + file, e);
			return new JsonArray();
		}
	}
	
	private void LoadReputations(JsonArray jsonRoot)
	{
	    if(jsonRoot == null || jsonRoot.size() <= 0) return;
	    
		int i = -1;
		
		for(JsonElement e : jsonRoot)
		{
		    if(!(e instanceof JsonObject)) continue;
		    JsonObject jRep = e.getAsJsonObject();
		    
		    String repName = "Reputation(" + i + ")";
		    if(jRep.has("Name")) repName = JsonHelper.GetString(jRep, "Name", repName);
		    if(jRep.has("name")) repName = JsonHelper.GetString(jRep, "name", repName);
		    
		    String repId = "" + (++i);
		    if(jRep.has("Id")) repId = JsonHelper.GetNumber(jRep, "Id", i).toString();
		    if(jRep.has("id")) repId = JsonHelper.GetString(jRep, "id", repId);
		    
		    
		    HQMRep repObj = new HQMRep(repName);
		    
		    JsonArray mrkAry = null;
		    if(jRep.has("Markers")) mrkAry = JsonHelper.GetArray(jRep, "Markers");
		    if(mrkAry == null) mrkAry = JsonHelper.GetArray(jRep, "markers");
		    
		    for(int m = 0; m < mrkAry.size(); m++)
            {
                JsonElement e2 = mrkAry.get(m);
                if(!(e2 instanceof JsonObject)) continue;
                
                JsonObject jMark = e2.getAsJsonObject();
                
                int mId = m;
                if(jMark.has("Id")) mId = JsonHelper.GetNumber(jMark, "Id", mId).intValue();
                
                int mVal = 0;
                if(jMark.has("Value")) mVal = JsonHelper.GetNumber(jMark, "Value", mVal).intValue();
                if(jMark.has("value")) mVal = JsonHelper.GetNumber(jMark, "value", mVal).intValue();
                
                repObj.addMarker(mId, mVal);
            }
		    
			reputations.put(repId, repObj);
		}
	}
	
	private IQuest GetNewQuest(String oldID, IQuestDatabase qdb)
	{
		if(idMap.containsKey(oldID))
		{
			return idMap.get(oldID);
		} else
		{
			IQuest quest = qdb.createNew(qdb.nextID());
			idMap.put(oldID, quest);
			return quest;
		}
	}
	
	private void ImportQuestLine(IQuestDatabase questDB, IQuestLineDatabase lineDB, JsonObject json)
	{
		IQuestLine questLine = lineDB.createNew(lineDB.nextID());
        questLine.setProperty(NativeProps.NAME, JsonHelper.GetString(json, "name", "HQM Quest Line"));
		questLine.setProperty(NativeProps.DESC, JsonHelper.GetString(json, "description", "No description"));
		
		LoadReputations(JsonHelper.GetArray(json, "reputations"));
		
		JsonArray qlJson = JsonHelper.GetArray(json, "quests");
		
		List<String> loadedQuests = new ArrayList<>(); // Just in case we have duplicate named quests
		
		for(int i = 0; i < qlJson.size(); i++)
		{
			JsonElement element = qlJson.get(i);
			
			if(element == null || !element.isJsonObject())
			{
				continue;
			}
			
			JsonObject jQuest = element.getAsJsonObject();
			
			String name = JsonHelper.GetString(jQuest, "name", "HQM Quest");
			String idName = jQuest.has("uuid")? JsonHelper.GetString(jQuest, "uuid", name) : name;
			
			if(loadedQuests.contains(idName))
			{
				int n = 1;
				while(loadedQuests.contains(idName + " (" + n + ")"))
				{
					n++;
				}
                BetterQuesting.logger.log(Level.WARN, "Found duplicate quest " + name + ". Any quests with this pre-requisite will need repair!");
				idName = name + " (" + n + ")";
			}
			
			loadedQuests.add(idName);
			IQuest quest = GetNewQuest(idName, questDB);
			
			quest.setProperty(NativeProps.NAME, name);
			quest.setProperty(NativeProps.DESC, JsonHelper.GetString(jQuest, "description", "No Description"));
			BigItemStack tmp = HQMUtilities.HQMStackT1(JsonHelper.GetObject(jQuest, "icon"));
			
			if(tmp != null)
			{
				quest.setProperty(NativeProps.ICON, tmp);
			} else
			{
				quest.setProperty(NativeProps.ICON, new BigItemStack(Items.NETHER_STAR));
			}
			
			if(json.has("repeat")) // Assuming this is in Minecraft time
			{
				JsonObject jRpt = JsonHelper.GetObject(jQuest, "repeat");
				int rTime = 0;
				rTime += JsonHelper.GetNumber(jRpt, "days", 0).intValue() * 24000;
				rTime += JsonHelper.GetNumber(jRpt, "hours", 0).intValue() * 1000;
				quest.setProperty(NativeProps.REPEAT_TIME, rTime);
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
				
				IQuest preReq = GetNewQuest(id, questDB);
				addReq(quest, questDB.getID(preReq));
			}
			
			for(JsonElement er : JsonHelper.GetArray(jQuest, "optionlinks"))
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
				
				IQuest preReq = GetNewQuest(id, questDB);
				addReq(quest, questDB.getID(preReq));
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
                    BetterQuesting.logger.warn("Unsupported HQM task \"" + tType + "\"! Skipping...");
					continue;
				}
				
				ITask[] tsks = taskConverters.get(tType).apply(jTask);
				
				if(tsks != null && tsks.length > 0)
				{
					IDatabaseNBT<ITask, NBTTagList, NBTTagList> taskReg = quest.getTasks();
					for(ITask t : tsks) taskReg.add(taskReg.nextID(), t);
				}
			}
			
			for(Entry<String,Function<JsonElement, IReward[]>> entry : rewardConverters.entrySet())
			{
				if(!jQuest.has(entry.getKey()))
				{
					continue;
				}
				
				IReward[] rews = entry.getValue().apply(jQuest.get(entry.getKey()));
				
				if(rews != null && rews.length > 0)
				{
					IDatabaseNBT<IReward, NBTTagList, NBTTagList> rewardReg = quest.getRewards();
					for(IReward r : rews)
					{
						rewardReg.add(rewardReg.nextID(), r);
					}
				}
			}
			
			if(questLine.getValue(questDB.getID(quest)) != null)
			{
                BetterQuesting.logger.log(Level.WARN, "Tried to add duplicate quest " + quest + " to quest line " + questLine.getUnlocalisedName());
			} else
			{
			    final int qleX = JsonHelper.GetNumber(jQuest, "x", 0).intValue();
			    final int qleY = JsonHelper.GetNumber(jQuest, "y", 0).intValue();
			    final boolean bigIcon = JsonHelper.GetBoolean(jQuest, "bigicon", false);
			    
			    IQuestLineEntry qle = questLine.createNew(questDB.getID(quest));
			    int size = bigIcon ? 32 : 24;
			    qle.setSize(size, size);
			    qle.setPosition(qleX, qleY);
			}
		}
	}
    
    private boolean containsReq(IQuest quest, int id)
    {
        for(int reqID : quest.getRequirements()) if(id == reqID) return true;
        return false;
    }
    
    private void addReq(IQuest quest, int id)
    {
        if(containsReq(quest, id)) return;
        int[] orig = quest.getRequirements();
        int[] added = Arrays.copyOf(orig, orig.length + 1);
        added[orig.length] = id;
        quest.setRequirements(added);
    }
	
	static
	{
		taskConverters.put("DETECT", new HQMTaskDetect(false)::convertTask);
		taskConverters.put("CONSUME", new HQMTaskDetect(true)::convertTask);
		taskConverters.put("CONSUME_QDS", new HQMTaskDetect(true)::convertTask);
		taskConverters.put("KILL", new HQMTaskKill()::convertTask);
		taskConverters.put("LOCATION", new HQMTaskLocation()::convertTask);
		taskConverters.put("CRAFT", new HQMTaskCraft()::convertTask);
		taskConverters.put("TAME", new HQMTaskTame()::convertTask);
		taskConverters.put("ADVANCEMENT", new HQMTaskAdvancement()::convertTask);
		taskConverters.put("BLOCK_BREAK", new HQMTaskBlockBreak()::convertTask);
		taskConverters.put("BLOCK_PLACE", new HQMTaskBlockPlace()::convertTask);
		taskConverters.put("REPUTATION", new HQMTaskReputaion()::convertTask);
		
		rewardConverters.put("reward", new HQMRewardStandard()::convertReward);
		rewardConverters.put("rewardchoice", new HQMRewardChoice()::convertReward);
		rewardConverters.put("reputationrewards", new HQMRewardReputation()::convertReward);
		rewardConverters.put("commandrewards", new HQMRewardCommand()::convertReward);
	}
}
