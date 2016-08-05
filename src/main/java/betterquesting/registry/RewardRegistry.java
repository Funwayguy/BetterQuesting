package betterquesting.registry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import net.minecraft.util.ResourceLocation;
import org.apache.logging.log4j.Level;
import betterquesting.api.quests.rewards.IRewardBase;
import betterquesting.api.quests.rewards.IRewardFactory;
import betterquesting.api.registry.IRewardRegistry;
import betterquesting.core.BetterQuesting;

public class RewardRegistry implements IRewardRegistry
{
	public static final RewardRegistry INSTANCE = new RewardRegistry();
	
	private HashMap<ResourceLocation, IRewardFactory<? extends IRewardBase>> rewardRegistry = new HashMap<ResourceLocation, IRewardFactory<? extends IRewardBase>>();
	
	private RewardRegistry()
	{
	}
	
	@Override
	public void registerReward(IRewardFactory<? extends IRewardBase> factory)
	{
		if(factory == null)
		{
			throw new NullPointerException("Tried to register null reward");
		} else if(factory.getRegistryName() == null)
		{
			throw new IllegalArgumentException("Tried to register a reward with a null name: " + factory.getClass());
		}
		
		if(rewardRegistry.containsKey(factory.getRegistryName()) || rewardRegistry.containsValue(factory))
		{
			throw new IllegalArgumentException("Cannot register dupliate reward type: " + factory.getRegistryName());
		}
		
		rewardRegistry.put(factory.getRegistryName(), factory);
	}
	
	@Override
	public IRewardFactory<? extends IRewardBase> getFactory(ResourceLocation registryName)
	{
		return rewardRegistry.get(registryName);
	}
	
	@Override
	public List<IRewardFactory<? extends IRewardBase>> getAll()
	{
		return new ArrayList<IRewardFactory<? extends IRewardBase>>(rewardRegistry.values());
	}
	
	@Override
	public IRewardBase createReward(ResourceLocation registryName)
	{
		try
		{
			IRewardFactory<? extends IRewardBase> reward = getFactory(registryName);
			
			if(reward == null)
			{
				BetterQuesting.logger.log(Level.ERROR, "Tried to load missing reward type '" + registryName + "'! Are you missing an expansion pack?");
				return null;
			}
			
			return reward.CreateReward();
		} catch(Exception e)
		{
			BetterQuesting.logger.log(Level.ERROR, "Unable to instatiate reward: " + registryName, e);
			return null;
		}
	}
}
