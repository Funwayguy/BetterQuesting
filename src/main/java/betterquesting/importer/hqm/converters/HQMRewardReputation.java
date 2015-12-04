package betterquesting.importer.hqm.converters;

import java.util.ArrayList;
import betterquesting.importer.hqm.HQMImporter;
import betterquesting.quests.rewards.RewardBase;
import betterquesting.quests.rewards.RewardScoreboard;
import betterquesting.utils.JsonHelper;
import com.google.gson.JsonElement;

public class HQMRewardReputation extends HQMReward
{
	@Override
	public ArrayList<RewardBase> Convert(JsonElement json)
	{
		ArrayList<RewardBase> rList = new ArrayList<RewardBase>();
		
		if(json == null || !json.isJsonArray())
		{
			return null;
		}
		
		for(JsonElement je : json.getAsJsonArray())
		{
			if(je == null || !je.isJsonObject())
			{
				continue;
			}
			
			int index = JsonHelper.GetNumber(je.getAsJsonObject(), "reputation", 0).intValue();
			int value = JsonHelper.GetNumber(je.getAsJsonObject(), "value", 1).intValue();
			String name = HQMImporter.reputations.containsKey(index)? HQMImporter.reputations.get(index) : "Reputation (" + index + ")";
			RewardScoreboard reward = new RewardScoreboard();
			reward.score = name;
			reward.value = value;
			rList.add(reward);
		}
		
		return rList;
	}
}
