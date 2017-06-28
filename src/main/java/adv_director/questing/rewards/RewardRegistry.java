package adv_director.questing.rewards;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import net.minecraft.util.ResourceLocation;
import org.apache.logging.log4j.Level;
import adv_director.api.misc.IFactory;
import adv_director.api.placeholders.rewards.FactoryRewardPlaceholder;
import adv_director.api.questing.rewards.IReward;
import adv_director.api.questing.rewards.IRewardRegistry;
import adv_director.core.AdvDirector;

public class RewardRegistry implements IRewardRegistry
{
	public static final RewardRegistry INSTANCE = new RewardRegistry();
	
	private HashMap<ResourceLocation, IFactory<? extends IReward>> rewardRegistry = new HashMap<ResourceLocation, IFactory<? extends IReward>>();
	
	private RewardRegistry()
	{
	}
	
	@Override
	public void registerReward(IFactory<? extends IReward> factory)
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
	public IFactory<? extends IReward> getFactory(ResourceLocation registryName)
	{
		return rewardRegistry.get(registryName);
	}
	
	@Override
	public List<IFactory<? extends IReward>> getAll()
	{
		return new ArrayList<IFactory<? extends IReward>>(rewardRegistry.values());
	}
	
	@Override
	public IReward createReward(ResourceLocation registryName)
	{
		try
		{
			IFactory<? extends IReward> factory = null;
			
			if(FactoryRewardPlaceholder.INSTANCE.getRegistryName().equals(registryName))
			{
				factory = FactoryRewardPlaceholder.INSTANCE;
			} else
			{
				factory = getFactory(registryName);
			}
			
			if(factory == null)
			{
				AdvDirector.logger.log(Level.ERROR, "Tried to load missing reward type '" + registryName + "'! Are you missing an expansion pack?");
				return null;
			}
			
			return factory.createNew();
		} catch(Exception e)
		{
			AdvDirector.logger.log(Level.ERROR, "Unable to instatiate reward: " + registryName, e);
			return null;
		}
	}
}
