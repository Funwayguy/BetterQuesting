package betterquesting.questing.rewards.factory;

import betterquesting.api.questing.rewards.IReward;
import betterquesting.api2.registry.IFactoryData;
import betterquesting.core.BetterQuesting;
import betterquesting.questing.rewards.RewardRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

public class FactoryRewardRecipe implements IFactoryData<IReward, NBTTagCompound>
{
	public static final FactoryRewardRecipe INSTANCE = new FactoryRewardRecipe();
	
	@Override
	public ResourceLocation getRegistryName()
	{
		return new ResourceLocation(BetterQuesting.MODID_STD, "recipe");
	}

	@Override
	public RewardRecipe createNew()
	{
		return new RewardRecipe();
	}

	@Override
	public RewardRecipe loadFromData(NBTTagCompound json)
	{
		RewardRecipe reward = new RewardRecipe();
		reward.readFromNBT(json);
		return reward;
	}
	
}
