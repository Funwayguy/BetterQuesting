package betterquesting.api.placeholders.rewards;

import betterquesting.api2.registry.IFactoryData;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;

public class FactoryRewardPlaceholder implements IFactoryData<RewardPlaceholder, CompoundNBT>
{
	public static final FactoryRewardPlaceholder INSTANCE = new FactoryRewardPlaceholder();
	
	private final ResourceLocation ID = new ResourceLocation("betterquesting:placeholder");
	
	private FactoryRewardPlaceholder()
	{
	}
	
	@Override
	public ResourceLocation getRegistryName()
	{
		return ID;
	}
	
	@Override
	public RewardPlaceholder createNew()
	{
		return new RewardPlaceholder();
	}
	
	@Override
	public RewardPlaceholder loadFromData(CompoundNBT nbt)
	{
		RewardPlaceholder reward = createNew();
		reward.readFromNBT(nbt);
		return reward;
	}
}
