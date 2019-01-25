package betterquesting.api.questing.rewards;

import betterquesting.api.misc.IFactory;
import net.minecraft.util.ResourceLocation;

import java.util.List;

public interface IRewardRegistry
{
	void registerReward(IFactory<? extends IReward> factory);
	IFactory<? extends IReward> getFactory(ResourceLocation name);
	List<IFactory<? extends IReward>> getAll();
	IReward createReward(ResourceLocation name);
}
