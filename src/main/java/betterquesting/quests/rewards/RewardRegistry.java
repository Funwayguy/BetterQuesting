package betterquesting.quests.rewards;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import net.minecraft.util.ResourceLocation;
import org.apache.logging.log4j.Level;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.ModContainer;
import betterquesting.core.BetterQuesting;

public class RewardRegistry
{
	static HashMap<ResourceLocation, Class <? extends RewardBase>> rewardRegistry = new HashMap<ResourceLocation, Class<? extends RewardBase>>();
	
	@Deprecated
	public static void RegisterReward(Class<? extends RewardBase> reward, String registeryName)
	{
		try
		{
			ModContainer mod = Loader.instance().activeModContainer();
			
			if(registeryName.contains(":"))
			{
				throw new IllegalArgumentException("Illegal character(s) used in reward ID name");
			} else if(mod == null)
			{
				throw new IllegalStateException("Tried to register a reward without a vialid mod instance");
			}
			
			RegisterReward(reward, new ResourceLocation(mod.getModId() + ":" + registeryName));
		} catch(Exception e)
		{
			BetterQuesting.logger.log(Level.ERROR, "An error occured while trying to register reward", e);
		}
	}
	
	public static void RegisterReward(Class<? extends RewardBase> reward, ResourceLocation registryName)
	{
		try
		{
			if(reward == null)
			{
				throw new NullPointerException("Tried to register null reward");
			} else if(registryName == null)
			{
				throw new IllegalArgumentException("Tried to register a reward with a null name");
			}
			
			try
			{
				reward.getDeclaredConstructor();
			} catch(NoSuchMethodException e)
			{
				throw new NoSuchMethodException("Reward is missing a default constructor with 0 arguemnts");
			}
			
			if(rewardRegistry.containsKey(registryName) || rewardRegistry.containsValue(reward))
			{
				throw new IllegalArgumentException("Cannot register dupliate reward type '" + registryName + "'");
			}
			
			rewardRegistry.put(registryName, reward);
		} catch(Exception e)
		{
			BetterQuesting.logger.log(Level.ERROR, "An error occured while trying to register reward", e);
		}
	}
	
	@Deprecated
	public static String GetID(Class<? extends RewardBase> reward)
	{
		ResourceLocation loc = GetRegisteredName(reward);
		return loc == null? null : loc.toString();
	}
	
	public static ResourceLocation GetRegisteredName(Class<? extends RewardBase> reward)
	{
		for(Entry<ResourceLocation,Class<? extends RewardBase>> set : rewardRegistry.entrySet())
		{
			if(set.getValue() == reward)
			{
				return set.getKey();
			}
		}
		
		return null;
	}
	
	@Deprecated
	public static Class<? extends RewardBase> GetReward(String registryName)
	{
		if(registryName == null)
		{
			return null;
		}
		
		return GetReward(new ResourceLocation(registryName));
	}
	
	public static Class<? extends RewardBase> GetReward(ResourceLocation registryName)
	{
		return rewardRegistry.get(registryName);
	}
	
	@Deprecated
	public static ArrayList<String> GetTypeList()
	{
		ArrayList<String> list = new ArrayList<String>();
		
		for(ResourceLocation loc : rewardRegistry.keySet())
		{
			list.add(loc.toString());
		}
		
		return list;
	}
	
	public static ArrayList<ResourceLocation> GetNameList()
	{
		return new ArrayList<ResourceLocation>(rewardRegistry.keySet());
	}
	
	@Deprecated
	public static RewardBase InstatiateReward(String registryName)
	{
		if(registryName == null)
		{
			return null;
		}
		
		return InstatiateReward(new ResourceLocation(registryName));
	}
	
	public static RewardBase InstatiateReward(ResourceLocation registryName)
	{
		try
		{
			Class<? extends RewardBase> reward = GetReward(registryName);
			
			if(reward == null)
			{
				BetterQuesting.logger.log(Level.ERROR, "Tried to load missing reward type '" + registryName + "'! Are you missing an expansion pack?");
				return null;
			}
			
			return reward.newInstance();
		} catch(Exception e)
		{
			BetterQuesting.logger.log(Level.ERROR, "Unable to instatiate reward: " + registryName, e);
			return null;
		}
	}
}
