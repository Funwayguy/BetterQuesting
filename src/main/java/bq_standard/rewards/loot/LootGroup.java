package bq_standard.rewards.loot;

import java.util.ArrayList;
import java.util.Random;
import betterquesting.utils.BigItemStack;
import betterquesting.utils.JsonHelper;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class LootGroup
{
	public String name = "Loot Group";
	public int weight = 1;
	public ArrayList<LootEntry> lootEntry = new ArrayList<LootEntry>();
	
	public ArrayList<BigItemStack> getRandomReward(Random rand)
	{
		int total = getTotalWeight();
		float r = rand.nextFloat() * total;
		int cnt = 0;
		
		for(LootEntry entry : lootEntry)
		{
			cnt += entry.weight;
			if(cnt >= r)
			{
				return entry.items;
			}
		}
		
		return new ArrayList<BigItemStack>();
	}
	
	public int getTotalWeight()
	{
		int i = 0;
		
		for(LootEntry entry : lootEntry)
		{
			i += entry.weight;
		}
		
		return i;
	}
	
	public void readFromJson(JsonObject json)
	{
		name = JsonHelper.GetString(json, "name", "Loot Group");
		weight = JsonHelper.GetNumber(json, "weight", 1).intValue();
		weight = Math.max(1, weight);
		
		lootEntry.clear();
		JsonArray jRew = JsonHelper.GetArray(json, "rewards");
		for(JsonElement entry : jRew)
		{
			if(entry == null || !entry.isJsonObject())
			{
				continue;
			}
			
			LootEntry loot = new LootEntry();
			loot.readFromJson(entry.getAsJsonObject());
			lootEntry.add(loot);
		}
	}
	
	public void writeToJson(JsonObject json)
	{
		json.addProperty("name", name);
		json.addProperty("weight", weight);
		
		JsonArray jRew = new JsonArray();
		for(LootEntry entry : lootEntry)
		{
			if(entry == null)
			{
				continue;
			}
			
			JsonObject jLoot = new JsonObject();
			entry.writeToJson(jLoot);
			jRew.add(jLoot);
		}
		json.add("rewards", jRew);
	}
	
	public static class LootEntry
	{
		public int weight = 1;
		public ArrayList<BigItemStack> items = new ArrayList<BigItemStack>();
		
		void readFromJson(JsonObject json)
		{
			weight = JsonHelper.GetNumber(json, "weight", 0).intValue();
			weight = Math.max(1, weight);
			
			items.clear();
			JsonArray jItm = JsonHelper.GetArray(json, "items");
			for(JsonElement entry : jItm)
			{
				if(entry == null || !entry.isJsonObject())
				{
					continue;
				}
				
				BigItemStack stack = JsonHelper.JsonToItemStack(entry.getAsJsonObject());
				
				if(stack != null)
				{
					items.add(stack);
				}
			}
		}
		
		void writeToJson(JsonObject json)
		{
			json.addProperty("weight", weight);
			
			JsonArray jItm = new JsonArray();
			for(BigItemStack stack : items)
			{
				jItm.add(JsonHelper.ItemStackToJson(stack, new JsonObject()));
			}
			json.add("items", jItm);
		}
	}
}
