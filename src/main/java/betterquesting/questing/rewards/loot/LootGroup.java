package betterquesting.questing.rewards.loot;

import betterquesting.api.utils.BigItemStack;
import betterquesting.api.utils.JsonHelper;
import betterquesting.api2.storage.DBEntry;
import betterquesting.api2.storage.INBTSaveLoad;
import betterquesting.api2.storage.SimpleDatabase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class LootGroup extends SimpleDatabase<LootGroup.LootEntry> implements INBTSaveLoad<NBTTagCompound>
{
	public String name = "Loot Group";
	public int weight = 1;
	
	public List<BigItemStack> getRandomReward(Random rand)
	{
		int total = getTotalWeight();
		float r = rand.nextFloat() * total;
		int cnt = 0;
		
		for(DBEntry<LootEntry> entry : getEntries())
		{
			cnt += entry.getValue().weight;
			if(cnt >= r)
			{
				return entry.getValue().items;
			}
		}
		
		return Collections.emptyList();
	}
	
	public int getTotalWeight()
	{
		int i = 0;
		
		for(DBEntry<LootEntry> entry : getEntries())
		{
			i += entry.getValue().weight;
		}
		
		return i;
	}
	
	@Override
	public void readFromNBT(NBTTagCompound tag)
	{
	    this.reset();
	    
		name = tag.getString("name");
		weight = Math.max(tag.getInteger("weight"), 1);
		
		// Old entries that were never given IDs
		List<LootEntry> legacyEntry = new ArrayList<>();
		
		NBTTagList jRew = tag.getTagList("rewards", 10);
		for(int i = 0; i < jRew.tagCount(); i++)
		{
			NBTTagCompound entry = jRew.getCompoundTagAt(i);
			int id = entry.hasKey("ID", 99) ? entry.getInteger("ID") : -1;
			
			LootEntry loot = new LootEntry();
			loot.readFromNBT(entry);
			
			if(id >= 0)
            {
                this.add(id, loot);
            } else
            {
                legacyEntry.add(loot);
            }
		}
		
		for(LootEntry entry : legacyEntry)
        {
            this.add(this.nextID(), entry);
        }
	}
	
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound tag)
	{
		tag.setString("name", name);
		tag.setInteger("weight", weight);
		
		NBTTagList jRew = new NBTTagList();
		for(DBEntry<LootEntry> entry : getEntries())
		{
			if(entry == null) continue;
			
			NBTTagCompound jLoot = entry.getValue().writeToNBT(new NBTTagCompound());
			jLoot.setInteger("ID", entry.getID());
			jRew.appendTag(jLoot);
		}
		tag.setTag("rewards", jRew);
		
		return tag;
	}
	
	public static class LootEntry implements INBTSaveLoad<NBTTagCompound>
	{
		public int weight = 1;
		public final List<BigItemStack> items = new ArrayList<>();
		
		@Override
		public void readFromNBT(NBTTagCompound json)
		{
			weight = json.getInteger("weight");
			weight = Math.max(1, weight);
			
			items.clear();
			NBTTagList jItm = json.getTagList("items", 10);
			for(int i = 0; i < jItm.tagCount(); i++)
			{
				items.add(JsonHelper.JsonToItemStack(jItm.getCompoundTagAt(i)));
			}
		}
		
		@Override
		public NBTTagCompound writeToNBT(NBTTagCompound tag)
		{
			tag.setInteger("weight", weight);
			
			NBTTagList jItm = new NBTTagList();
			for(BigItemStack stack : items)
			{
				jItm.appendTag(JsonHelper.ItemStackToJson(stack, new NBTTagCompound()));
			}
			tag.setTag("items", jItm);
			
			return tag;
		}
	}
}
