package betterquesting.registry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import net.minecraft.util.ResourceLocation;
import org.apache.logging.log4j.Level;
import betterquesting.api.quests.rewards.IReward;
import betterquesting.api.registry.IRewardRegistry;
import betterquesting.api.utils.IFactory;
import betterquesting.core.BetterQuesting;

public class RewardRegistry implements IRewardRegistry
{
	public static final RewardRegistry INSTANCE = new RewardRegistry();
	
	private HashMap<ResourceLocation, IFactory<IReward>> rewardRegistry = new HashMap<ResourceLocation, IFactory<IReward>>();
	
	private RewardRegistry()
	{
	}
	
	@Override
	public void registerReward(IFactory<IReward> factory)
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
	public IFactory<IReward> getFactory(ResourceLocation registryName)
	{
		return rewardRegistry.get(registryName);
	}
	
	@Override
	public List<IFactory<IReward>> getAll()
	{
		return new ArrayList<IFactory<IReward>>(rewardRegistry.values());
	}
	
	@Override
	public IReward createReward(ResourceLocation registryName)
	{
		try
		{
			IFactory<? extends IReward> reward = getFactory(registryName);
			
			if(reward == null)
			{
				BetterQuesting.logger.log(Level.ERROR, "Tried to load missing reward type '" + registryName + "'! Are you missing an expansion pack?");
				return null;
			}
			
			return reward.createNew();
		} catch(Exception e)
		{
			BetterQuesting.logger.log(Level.ERROR, "Unable to instatiate reward: " + registryName, e);
			return null;
		}
	}
}
