package betterquesting.api.registry;

import java.util.List;
import net.minecraft.util.ResourceLocation;
import betterquesting.api.quests.rewards.IRewardBase;
import betterquesting.api.quests.rewards.IRewardFactory;

public interface IRewardRegistry
{
	public void registerReward(IRewardFactory<? extends IRewardBase> factory);
	public IRewardFactory<? extends IRewardBase> getFactory(ResourceLocation name);
	public List<IRewardFactory<? extends IRewardBase>> getAll();
	public IRewardBase createReward(ResourceLocation name);
}
