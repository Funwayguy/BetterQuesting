package betterquesting.importer.hqm.converters;

import java.util.ArrayList;
import betterquesting.importer.hqm.HQMImporter;
import betterquesting.quests.rewards.RewardBase;
import betterquesting.quests.rewards.RewardItem;
import com.google.gson.JsonElement;

public class HQMRewardStandard extends HQMReward
{
	@Override
	public ArrayList<RewardBase> Convert(JsonElement json)
	{
		ArrayList<RewardBase> rList = new ArrayList<RewardBase>();
		
		if(json == null || !json.isJsonArray())
		{
			return null;
		}
		
		RewardItem reward = new RewardItem();
		rList.add(reward);
		
		for(JsonElement je : json.getAsJsonArray())
		{
			if(je == null || !je.isJsonObject())
			{
				continue;
			}
			
			reward.rewards.add(HQMImporter.HQMStackT1(je.getAsJsonObject()));
		}
		
		return rList;
	}
	
}
