package betterquesting.importer.hqm;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import org.apache.logging.log4j.Level;
import betterquesting.core.BetterQuesting;
import betterquesting.importer.hqm.converters.*;
import betterquesting.quests.QuestDatabase;
import betterquesting.quests.QuestInstance;
import betterquesting.quests.QuestLine;
import betterquesting.quests.rewards.RewardBase;
import betterquesting.quests.tasks.TaskBase;
import betterquesting.utils.JsonHelper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class HQMImporter
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
					BetterQuesting.logger.log(Level.ERROR, "An error occured during import", e);
					continue;
				}
				
				if(json != null)
				{
					ImportQuestLine(json);
				}
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
		BetterQuesting.logger.log(Level.INFO, "Beginning import...");
		
		QuestLine questLine = new QuestLine();
		questLine.name = JsonHelper.GetString(json, "name", "HQM Quest Line");
		questLine.description = JsonHelper.GetString(json, "description", "No description");
		
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
				BetterQuesting.logger.log(Level.WARN, "Found duplicate named quest " + name + ". Any quests with this pre-requisite will need repair!");
				idName = name + " (" + n + ")";
			}
			
			loadedQuests.add(idName);
			QuestInstance quest = GetNewQuest(idName);
			
			quest.name = name;
			quest.description = JsonHelper.GetString(jQuest, "description", "No Description");
			ArrayList<ItemStack> tmp = HQMStackT1(JsonHelper.GetObject(jQuest, "icon"));
			
			if(tmp.size() > 0)
			{
				quest.itemIcon = tmp.get(0);
			} else
			{
				quest.itemIcon = new ItemStack(Items.nether_star);
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
					BetterQuesting.logger.log(Level.WARN, "Unidentified HQM task '" + tType + "'! Please report this so that it can be supported in future builds");
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
				BetterQuesting.logger.log(Level.WARN, "Tried to add duplicate quest " + quest + " to quest line " + questLine.name);
			} else
			{
				questLine.questList.add(quest);
			}
		}
		
		questLine.BuildTree();
		QuestDatabase.questLines.add(questLine);
		QuestDatabase.UpdateClients();
	}
	
	/**
	 * Get HQM formatted item, Type 1
	 */
	public static ArrayList<ItemStack> HQMStackT1(JsonObject json) // This can return multiple stacks in the event the stack size exceeds 127
	{
		ArrayList<ItemStack> list = new ArrayList<ItemStack>();
		
		String iID = JsonHelper.GetString(json, "id", "minecraft:stone");
		Item item = (Item)Item.itemRegistry.getObject(iID);
		int amount = JsonHelper.GetNumber(json, "amount", 1).intValue();
		int damage = JsonHelper.GetNumber(json, "damage", 0).intValue();
		NBTTagCompound tags = null;
		
		if(json.has("nbt"))
		{
			try
			{
				String rawNbt = json.get("nbt").toString(); // Must use this method. Gson formatting will damage it otherwise
				
				// Hack job to fix backslashes (why are 2 Json formats being used in HQM?!)
				rawNbt = rawNbt.replaceFirst("\"", ""); // Delete first quote
				rawNbt = rawNbt.substring(0, rawNbt.length() - 1); // Delete last quote
				rawNbt = rawNbt.replace(":\\\"", ":\""); // Fix start of strings
				rawNbt = rawNbt.replace("\\\",", "\","); // Fix middle of lists
				rawNbt = rawNbt.replace("\\\"}", "\"}"); // Fix end of strings
				rawNbt = rawNbt.replace("\\\"]", "\"]"); // Fix end of lists
				rawNbt = rawNbt.replace("[\\\"", "[\""); // Fix start of lists
				rawNbt = rawNbt.replace("\\n", "\n");
				
				NBTBase nbt = JsonToNBT.func_150315_a(rawNbt);
				
				if(nbt != null && nbt instanceof NBTTagCompound)
				{
					tags = (NBTTagCompound)nbt;
				}
			} catch(Exception e)
			{
				BetterQuesting.logger.log(Level.ERROR, "Unable to convert HQM NBT data. This is likely a HQM Gson/Json formatting issue", e);
			}
		}
		
		if(item == null)
		{
			item = BetterQuesting.placeholder;
			NBTTagCompound tmp = new NBTTagCompound();
			if(tags != null)
			{
				tmp.setTag("orig_tag", tags);
			}
			tmp.setString("orig_id", iID);
			tags = tmp;
		}
		
		while(amount > 0)
		{
			int cur = Math.min(amount, 127);
			amount -= cur;
			ItemStack stack = new ItemStack(item, cur, damage);
			
			if(tags != null)
			{
				stack.stackTagCompound = (NBTTagCompound)tags.copy();
			}
			
			list.add(stack);
		}
		
		return list;
	}
	
	/**
	 * Get HQM formatted item, Type 2
	 */
	public static ArrayList<ItemStack> HQMStackT2(JsonObject rJson) // This can return multiple stacks in the event the stack size exceeds 127
	{
		JsonObject json = JsonHelper.GetObject(rJson, "item");
		ArrayList<ItemStack> list = new ArrayList<ItemStack>();
		
		String iID = JsonHelper.GetString(json, "id", "minecraft:stone");
		Item item = (Item)Item.itemRegistry.getObject(iID);
		int amount = JsonHelper.GetNumber(rJson, "required", 1).intValue();
		int damage = JsonHelper.GetNumber(json, "damage", 0).intValue();
		NBTTagCompound tags = null;
		
		if(json.has("nbt"))
		{
			try
			{
				String rawNbt = json.get("nbt").toString(); // Must use this method. Gson formatting will damage it otherwise
				
				// Hack job to fix backslashes (why are 2 Json formats being used in HQM?!)
				rawNbt = rawNbt.replaceFirst("\"", ""); // Delete first quote
				rawNbt = rawNbt.substring(0, rawNbt.length() - 1); // Delete last quote
				rawNbt = rawNbt.replace(":\\\"", ":\""); // Fix start of strings
				rawNbt = rawNbt.replace("\\\",", "\","); // Fix middle of lists
				rawNbt = rawNbt.replace("\\\"}", "\"}"); // Fix end of strings
				rawNbt = rawNbt.replace("\\\"]", "\"]"); // Fix end of lists
				rawNbt = rawNbt.replace("[\\\"", "[\""); // Fix start of lists
				rawNbt = rawNbt.replace("\\n", "\n");
				
				NBTBase nbt = JsonToNBT.func_150315_a(rawNbt);
				
				if(nbt != null && nbt instanceof NBTTagCompound)
				{
					tags = (NBTTagCompound)nbt;
				}
			} catch(Exception e)
			{
				BetterQuesting.logger.log(Level.ERROR, "Unable to convert HQM NBT data. This is likely a HQM Gson/Json formatting issue", e);
			}
		}
		
		if(item == null)
		{
			item = BetterQuesting.placeholder;
			NBTTagCompound tmp = new NBTTagCompound();
			if(tags != null)
			{
				tmp.setTag("orig_tag", tags);
			}
			tmp.setString("orig_id", iID);
			tags = tmp;
		}
		
		while(amount > 0)
		{
			int cur = Math.min(amount, 127);
			amount -= cur;
			ItemStack stack = new ItemStack(item, cur, damage);
			
			if(tags != null)
			{
				stack.stackTagCompound = (NBTTagCompound)tags.copy();
			}
			
			list.add(stack);
		}
		
		return list;
	}
	
	public static FluidStack HQMStackT3(JsonObject json)
	{
		Fluid fluid = FluidRegistry.getFluid(JsonHelper.GetString(json, "fluid", "water"));
		fluid = fluid != null? fluid : FluidRegistry.WATER;
		int amount = JsonHelper.GetNumber(json, "required", 1000).intValue();
		
		return new FluidStack(fluid, amount);
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
		rewardConverters.put("rewardChoice", new HQMRewardChoice());
	}
}
