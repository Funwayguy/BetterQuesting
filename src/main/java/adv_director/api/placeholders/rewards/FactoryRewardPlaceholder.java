package adv_director.api.placeholders.rewards;

import net.minecraft.util.ResourceLocation;
import adv_director.api.enums.EnumSaveType;
import adv_director.api.misc.IFactory;
import com.google.gson.JsonObject;

public class FactoryRewardPlaceholder implements IFactory<RewardPlaceholder>
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
	public RewardPlaceholder loadFromJson(JsonObject json)
	{
		RewardPlaceholder reward = createNew();
		reward.readFromJson(json, EnumSaveType.CONFIG);
		return reward;
	}
}
