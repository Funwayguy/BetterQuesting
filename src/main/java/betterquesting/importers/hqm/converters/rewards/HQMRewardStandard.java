package betterquesting.importers.hqm.converters.rewards;

import betterquesting.api.questing.rewards.IReward;
import betterquesting.importers.hqm.HQMUtilities;
import betterquesting.questing.rewards.RewardItem;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class HQMRewardStandard
{
	public IReward[] convertReward(JsonElement json)
	{
		if(!(json instanceof JsonArray)) return null;
		
		RewardItem reward = new RewardItem();
		for(JsonElement je : json.getAsJsonArray())
		{
			if(!(je instanceof JsonObject)) continue;
			reward.items.add(HQMUtilities.HQMStackT1(je.getAsJsonObject()));
		}
		
		return new IReward[]{reward};
	}
}
