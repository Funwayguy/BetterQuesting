package betterquesting.quests;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import net.minecraft.util.ResourceLocation;
import betterquesting.api.database.IJsonSaveLoad;
import betterquesting.api.database.IRegStorageBase;
import betterquesting.api.enums.EnumSaveType;
import betterquesting.api.quests.rewards.IReward;
import betterquesting.api.utils.JsonHelper;
import betterquesting.registry.RewardRegistry;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class RewardStorage implements IRegStorageBase<Integer,IReward>, IJsonSaveLoad<JsonArray>
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
	public JsonArray writeToJson(JsonArray json, EnumSaveType saveType)
	{
		if(saveType != EnumSaveType.CONFIG)
		{
			return json;
		}
		
		for(Entry<Integer,IReward> rew : database.entrySet())
		{
			ResourceLocation rewardID = rew.getValue().getFactoryID();
			
			JsonObject rJson = rew.getValue().writeToJson(new JsonObject(), EnumSaveType.CONFIG);
			rJson.addProperty("rewardID", rewardID.toString());
			rJson.addProperty("index", rew.getKey());
			json.add(rJson);
		}
		
		return json;
	}
	
	@Override
	public void readFromJson(JsonArray json, EnumSaveType saveType)
	{
		if(saveType != EnumSaveType.CONFIG)
		{
			return;
		}
		
		ArrayList<IReward> unassigned = new ArrayList<IReward>();
		
		for(JsonElement entry : json)
		{
			if(entry == null || !entry.isJsonObject())
			{
				continue;
			}
			
			JsonObject jsonReward = entry.getAsJsonObject();
			ResourceLocation loc = new ResourceLocation(JsonHelper.GetString(jsonReward, "rewardID", ""));
			int index = JsonHelper.GetNumber(jsonReward, "index", -1).intValue();
			IReward reward = RewardRegistry.INSTANCE.createReward(loc);
			
			if(reward != null)
			{
				reward.readFromJson(jsonReward, EnumSaveType.CONFIG);
				
				if(index >= 0)
				{
					add(reward, index);
				} else
				{
					unassigned.add(reward);
				}
			}
		}
		
		for(IReward r : unassigned)
		{
			add(r, nextKey());
		}
	}
}
