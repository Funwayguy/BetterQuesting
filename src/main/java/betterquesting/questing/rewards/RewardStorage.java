package betterquesting.questing.rewards;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;
import betterquesting.api.enums.EnumSaveType;
import betterquesting.api.misc.INBTSaveLoad;
import betterquesting.api.placeholders.rewards.RewardPlaceholder;
import betterquesting.api.questing.rewards.IReward;
import betterquesting.api.storage.IRegStorageBase;

public class RewardStorage implements IRegStorageBase<Integer,IReward>, INBTSaveLoad<NBTTagList>
{
	private final HashMap<Integer,IReward> database = new HashMap<Integer,IReward>();
	
	@Override
	public Integer nextKey()
	{
		int id = 0;
		
		while(database.containsKey(id))
		{
			id++;
		}
		
		return id;
	}
	
	@Override
	public boolean add(IReward obj, Integer id)
	{
		if(obj == null || database.containsKey(id) || database.containsKey(id))
		{
			return false;
		}
		
		database.put(id, obj);
		return true;
	}
	
	@Override
	public boolean removeKey(Integer id)
	{
		return database.remove(id) != null;
	}
	
	@Override
	public boolean removeValue(IReward reward)
	{
		return removeKey(getKey(reward));
	}
	
	@Override
	public IReward getValue(Integer id)
	{
		return database.get(id);
	}
	
	@Override
	public Integer getKey(IReward obj)
	{
		int id = -1;
		
		for(Entry<Integer,IReward> entry : database.entrySet())
		{
			if(entry.getValue() == obj)
			{
				return entry.getKey();
			}
		}
		
		return id;
	}
	
	@Override
	public List<IReward> getAllValues()
	{
		return new ArrayList<IReward>(database.values());
	}
	
	@Override
	public List<Integer> getAllKeys()
	{
		return new ArrayList<Integer>(database.keySet());
	}
	
	@Override
	public int size()
	{
		return database.size();
	}
	
	@Override
	public void reset()
	{
		database.clear();
	}
	
	@Override
	public NBTTagList writeToNBT(NBTTagList json, EnumSaveType saveType)
	{
		if(saveType != EnumSaveType.CONFIG)
		{
			return json;
		}
		
		for(Entry<Integer,IReward> rew : database.entrySet())
		{
			ResourceLocation rewardID = rew.getValue().getFactoryID();
			
			NBTTagCompound rJson = rew.getValue().writeToNBT(new NBTTagCompound(), EnumSaveType.CONFIG);
			rJson.setString("rewardID", rewardID.toString());
			rJson.setInteger("index", rew.getKey());
			json.appendTag(rJson);
		}
		
		return json;
	}
	
	@Override
	public void readFromNBT(NBTTagList json, EnumSaveType saveType)
	{
		if(saveType != EnumSaveType.CONFIG)
		{
			return;
		}
		
		database.clear();
		
		ArrayList<IReward> unassigned = new ArrayList<IReward>();
		
		for(int i = 0; i < json.tagCount(); i++)
		{
			NBTBase entry = json.get(i);
			
			if(entry == null || entry.getId() != 10)
			{
				continue;
			}
			
			NBTTagCompound jsonReward = (NBTTagCompound)entry;
			ResourceLocation loc = new ResourceLocation(jsonReward.getString("rewardID"));
			int index = jsonReward.hasKey("index", 99) ? jsonReward.getInteger("index") : -1;
			IReward reward = RewardRegistry.INSTANCE.createReward(loc);
			
			if(reward instanceof RewardPlaceholder)
			{
				NBTTagCompound jr2 = jsonReward.getCompoundTag("orig_data");
				ResourceLocation loc2 = new ResourceLocation(jr2.getString("rewardID"));
				IReward r2 = RewardRegistry.INSTANCE.createReward(loc2);
				
				if(r2 != null)
				{
					jsonReward = jr2;
					reward = r2;
				}
			}
			
			if(reward != null)
			{
				reward.readFromNBT(jsonReward, EnumSaveType.CONFIG);
				
				if(index >= 0)
				{
					add(reward, index);
				} else
				{
					unassigned.add(reward);
				}
			} else
			{
				RewardPlaceholder rph = new RewardPlaceholder();
				rph.setRewardData(jsonReward, EnumSaveType.CONFIG);
				
				if(index >= 0)
				{
					add(rph, index);
				} else
				{
					unassigned.add(rph);
				}
			}
		}
		
		for(IReward r : unassigned)
		{
			add(r, nextKey());
		}
	}
}
