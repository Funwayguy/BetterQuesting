package betterquesting.api.quests.rewards;

import net.minecraft.util.ResourceLocation;
import com.google.gson.JsonObject;

public interface IRewardFactory<T extends IRewardBase>
{
	public ResourceLocation getRegistryName();
	public T CreateReward();
	public T LoadReward(JsonObject json);
}
