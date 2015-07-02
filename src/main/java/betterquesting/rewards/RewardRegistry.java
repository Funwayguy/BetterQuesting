package betterquesting.rewards;

import java.util.ArrayList;
import java.util.HashMap;
import org.apache.logging.log4j.Level;
import betterquesting.core.BetterQuesting;
import cpw.mods.fml.common.Loader;

public class RewardRegistry
{
	static HashMap<String, Class <? extends RewardBase>> rewardRegistry = new HashMap<String, Class<? extends RewardBase>>();
	
	public static void RegisterReward(Class<? extends RewardBase> reward, Object mod, String idName)
	{
		try
		{
			if(reward == null)
			{
				throw new NullPointerException("Tried to register null reward");
			} else if(Loader.instance().getReversedModObjectList().containsKey(mod))
			{
				throw new IllegalArgumentException("Tried to register a reward without a vialid mod instance");
			}
			
			try
			{
				reward.getDeclaredConstructor();
			} catch(NoSuchMethodException e)
			{
				throw new NoSuchMethodException("Registered quest is missing a default constructor with 0 arguemnts");
			}
			
			String fullName = Loader.instance().getReversedModObjectList().get(mod).getModId() + ":" + idName;
			
			if(rewardRegistry.containsKey(fullName))
			{
				throw new IllegalStateException("Cannot register dupliate reward type '" + fullName + "'");
			}
			
			rewardRegistry.put(fullName, reward);
		} catch(Exception e)
		{
			BetterQuesting.logger.log(Level.ERROR, "An error occured while trying to register reward type:", e);
		}
	}
	
	public static String GetID(Class<? extends RewardBase> reward)
	{
		for(String idName : rewardRegistry.keySet())
		{
			if(rewardRegistry.get(idName) == reward)
			{
				return idName;
			}
		}
		
		return null;
	}
	
	public static Class<? extends RewardBase> GetQuest(String idName)
	{
		return rewardRegistry.get(idName);
	}
	
	public static ArrayList<String> GetTypeList()
	{
		return new ArrayList<String>(rewardRegistry.keySet());
	}
	
	public static RewardBase InstatiateReward(String idName)
	{
		try
		{
			return rewardRegistry.get(idName).newInstance();
		} catch(Exception e)
		{
			BetterQuesting.logger.log(Level.ERROR, "Unable to instatiate reward: " + idName, e);
			return null;
		}
	}
}
