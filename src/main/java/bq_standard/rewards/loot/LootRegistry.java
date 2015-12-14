package bq_standard.rewards.loot;

import java.io.File;
import java.util.ArrayList;
import java.util.Random;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.ChestGenHooks;
import net.minecraftforge.event.world.WorldEvent;
import betterquesting.core.BQ_Settings;
import betterquesting.utils.BigItemStack;
import betterquesting.utils.JsonHelper;
import betterquesting.utils.JsonIO;
import bq_standard.core.BQ_Standard;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

public class LootRegistry
{
	static ArrayList<LootGroup> lootGroups = new ArrayList<LootGroup>();
	
	public static void registerGroup(LootGroup group)
	{
		if(group == null || lootGroups.contains(group))
		{
			return;
		}
		
		lootGroups.add(group);
	}
	
	/**
	 * 
	 * @param weight A value between 0 and 1 that represents how common this reward is (i.e. higher values mean rarer loot)
	 * @param rand
	 * @return a loot group with the corresponding rarity of loot
	 */
	public static LootGroup getWeightedGroup(float weight, Random rand)
	{
		int total = getTotalWeight();
		float r = rand.nextFloat() * total/2F + weight*total/2F;
		int cnt = 0;
		System.out.println("Weight: " + r);
		
		for(LootGroup entry : lootGroups)
		{
			cnt += entry.weight;
			if(cnt >= r)
			{
				return entry;
			}
		}
		
		return null;
	}
	
	public static int getTotalWeight()
	{
		int i = 0;
		
		for(LootGroup group : lootGroups)
		{
			i += group.weight;
		}
		
		return i;
	}
	
	public static ArrayList<BigItemStack> getStandardLoot(Random rand)
	{
		ArrayList<BigItemStack> stacks = new ArrayList<BigItemStack>();
		
		int i = 1 + rand.nextInt(7);
		
		while(i > 0)
		{
			stacks.add(new BigItemStack(ChestGenHooks.getOneItem(ChestGenHooks.DUNGEON_CHEST, rand)));
			i--;
		}
		
		return stacks;
	}
	
	public static ArrayList<LootGroup> getGroups()
	{
		return lootGroups;
	}
	
	public static void writeToJson(JsonObject json)
	{
		JsonArray jRew = new JsonArray();
		for(LootGroup entry : lootGroups)
		{
			JsonObject jGrp = new JsonObject();
			entry.writeToJson(jGrp);
			jRew.add(jGrp);
		}
		json.add("groups", jRew);
	}
	
	public static void readFromJson(JsonObject json)
	{
		lootGroups.clear();
		for(JsonElement entry : JsonHelper.GetArray(json, "groups"))
		{
			if(entry == null || !entry.isJsonObject())
			{
				continue;
			}
			
			LootGroup group = new LootGroup();
			group.readFromJson(entry.getAsJsonObject());
			
			lootGroups.add(group);
		}
	}
	
	static File worldDir = null;
	
	@SubscribeEvent
	public void onWorldLoad(WorldEvent.Load event)
	{
		if(event.world.isRemote || worldDir != null)
		{
			return;
		}
		
		MinecraftServer server = MinecraftServer.getServer();
		
		if(BQ_Standard.proxy.isClient())
		{
			worldDir = server.getFile("saves/" + server.getFolderName());
		} else
		{
			worldDir = server.getFile(server.getFolderName());
		}
    	
    	File f1 = new File(worldDir, "QuestLoot.json");
		JsonObject j1 = new JsonObject();
		
		if(f1.exists())
		{
			j1 = JsonIO.ReadFromFile(f1);
		} else
		{
			f1 = server.getFile(BQ_Settings.defaultDir + "QuestLoot.json");
			
			if(f1.exists())
			{
				j1 = JsonIO.ReadFromFile(f1);
			}
		}
		
		readFromJson(j1);
	}
	
	@SubscribeEvent
	public void onWorldSave(WorldEvent.Save event)
	{
		if(!event.world.isRemote && worldDir != null && event.world.provider.dimensionId == 0)
		{
			JsonObject jsonQ = new JsonObject();
			writeToJson(jsonQ);
			JsonIO.WriteToFile(new File(worldDir, "QuestLoot.json"), jsonQ);
		}
	}
	
	@SubscribeEvent
	public void onWorldUnload(WorldEvent.Unload event)
	{
		if(!event.world.isRemote && !MinecraftServer.getServer().isServerRunning())
		{
			worldDir = null;
		}
	}
}
