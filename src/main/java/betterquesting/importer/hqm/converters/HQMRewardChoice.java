package betterquesting.importer.hqm.converters;

import java.util.ArrayList;
import betterquesting.importer.hqm.HQMImporter;
import betterquesting.quests.rewards.RewardBase;
import betterquesting.quests.rewards.RewardChoice;
import com.google.gson.JsonElement;

public class HQMRewardChoice extends HQMReward
{
	@Override
	public ArrayList<RewardBase> Convert(JsonElement json)
	{
		ArrayList<RewardBase> rList = new ArrayList<RewardBase>();
		
		if(json == null || !json.isJsonArray())
		{
			return rList;
		}
		
		RewardChoice reward = new RewardChoice();
		rList.add(reward);
		
		for(JsonElement je : json.getAsJsonArray())
		{
			if(je == null || !je.isJsonObject())
			{
				continue;
			}
			
			reward.choices.addAll(HQMImporter.HQMStackT1(je.getAsJsonObject()));
		}
		
		return rList;
	}
	
}
