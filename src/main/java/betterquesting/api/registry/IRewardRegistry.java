package betterquesting.api.registry;

import java.util.List;
import net.minecraft.util.ResourceLocation;
import betterquesting.api.quests.rewards.IReward;
import betterquesting.api.utils.IFactory;

public interface IRewardRegistry
{
	public void registerReward(IFactory<IReward> factory);
	public IFactory<IReward> getFactory(ResourceLocation name);
	public List<IFactory<IReward>> getAll();
	public IReward createReward(ResourceLocation name);
}
