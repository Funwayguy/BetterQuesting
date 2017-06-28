package adv_director.api.questing.rewards;

import java.util.List;
import net.minecraft.util.ResourceLocation;
import adv_director.api.misc.IFactory;

public interface IRewardRegistry
{
	public void registerReward(IFactory<? extends IReward> factory);
	public IFactory<? extends IReward> getFactory(ResourceLocation name);
	public List<IFactory<? extends IReward>> getAll();
	public IReward createReward(ResourceLocation name);
}
