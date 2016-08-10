package betterquesting.quests;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import net.minecraft.util.ResourceLocation;
import betterquesting.api.database.IJsonSaveLoad;
import betterquesting.api.database.IRegStorage;
import betterquesting.api.enums.EnumSaveType;
import betterquesting.api.quests.rewards.IRewardBase;
import betterquesting.api.utils.JsonHelper;
import betterquesting.registry.RewardRegistry;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class RewardStorage implements IRegStorage<IRewardBase>, IJsonSaveLoad<JsonArray>
{
	private final HashMap<Integer,IRewardBase> database = new HashMap<Integer,IRewardBase>();
	
	@Override
	public int nextID()
	{
		int id = 0;
		
		while(database.containsKey(id))
		{
			id++;
		}
		
		return id;
	}
	
	@Override
	public boolean add(IRewardBase obj, int id)
	{
		if(obj == null || database.containsKey(id) || database.containsKey(id))
		{
			return false;
		}
		
		database.put(id, obj);
		return true;
	}
	
	@Override
	public boolean remove(int id)
	{
		return database.remove(id) != null;
	}
	
	@Override
	public IRewardBase getValue(int id)
	{
		return database.get(id);
	}
	
	@Override
	public int getKey(IRewardBase obj)
	{
		int id = -1;
		
		for(Entry<Integer,IRewardBase> entry : database.entrySet())
		{
			if(entry.getValue() == obj)
			{
				return entry.getKey();
			}
		}
		
		return id;
	}
	
	@Override
	public List<IRewardBase> getAllValues()
	{
		return new ArrayList<IRewardBase>(database.values());
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
	public JsonArray writeToJson(JsonArray json, EnumSaveType saveType)
	{
		if(saveType != EnumSaveType.CONFIG)
		{
			return json;
		}
		
		for(Entry<Integer,IRewardBase> rew : database.entrySet())
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
		
		ArrayList<IRewardBase> unassigned = new ArrayList<IRewardBase>();
		
		for(JsonElement entry : json)
		{
			if(entry == null || !entry.isJsonObject())
			{
				continue;
			}
			
			JsonObject jsonReward = entry.getAsJsonObject();
			ResourceLocation loc = new ResourceLocation(JsonHelper.GetString(jsonReward, "rewardID", ""));
			int index = JsonHelper.GetNumber(jsonReward, "index", -1).intValue();
			IRewardBase reward = RewardRegistry.INSTANCE.createReward(loc);
			
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
		
		for(IRewardBase r : unassigned)
		{
			add(r, nextID());
		}
	}
}
