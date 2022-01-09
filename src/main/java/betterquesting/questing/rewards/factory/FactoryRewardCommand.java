package betterquesting.questing.rewards.factory;

import betterquesting.api.questing.rewards.IReward;
import betterquesting.api2.registry.IFactoryData;
import betterquesting.core.BetterQuesting;
import betterquesting.questing.rewards.RewardCommand;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

public class FactoryRewardCommand implements IFactoryData<IReward, NBTTagCompound>
{
	public static final FactoryRewardCommand INSTANCE = new FactoryRewardCommand();
	
	@Override
	public ResourceLocation getRegistryName()
	{
		return new ResourceLocation(BetterQuesting.MODID_STD, "command");
	}

	@Override
	public RewardCommand createNew()
	{
		return new RewardCommand();
	}

	@Override
	public RewardCommand loadFromData(NBTTagCompound json)
	{
		RewardCommand reward = new RewardCommand();
		reward.readFromNBT(json);
		return reward;
	}
	
}
