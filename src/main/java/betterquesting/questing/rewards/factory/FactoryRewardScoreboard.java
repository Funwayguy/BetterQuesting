package betterquesting.questing.rewards.factory;

import betterquesting.api.questing.rewards.IReward;
import betterquesting.api2.registry.IFactoryData;
import betterquesting.core.BetterQuesting;
import betterquesting.questing.rewards.RewardScoreboard;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

public class FactoryRewardScoreboard implements IFactoryData<IReward, NBTTagCompound>
{
	public static final FactoryRewardScoreboard INSTANCE = new FactoryRewardScoreboard();
	
	@Override
	public ResourceLocation getRegistryName()
	{
		return new ResourceLocation(BetterQuesting.MODID_STD, "scoreboard");
	}

	@Override
	public RewardScoreboard createNew()
	{
		return new RewardScoreboard();
	}

	@Override
	public RewardScoreboard loadFromData(NBTTagCompound json)
	{
		RewardScoreboard reward = new RewardScoreboard();
		reward.readFromNBT(json);
		return reward;
	}
	
}
