package betterquesting.importers.hqm.converters.rewards;

import betterquesting.api.questing.rewards.IReward;
import betterquesting.api.utils.JsonHelper;
import betterquesting.importers.hqm.HQMQuestImporter;
import betterquesting.importers.hqm.converters.HQMRep;
import betterquesting.questing.rewards.RewardScoreboard;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

public class HQMRewardReputation
{
	public IReward[] convertReward(JsonElement json)
	{
		if(!(json instanceof JsonArray)) return null;
		List<IReward> rList = new ArrayList<>();
		
		for(JsonElement je : json.getAsJsonArray())
		{
			if(!(je instanceof JsonObject)) continue;
			JsonObject jRep = je.getAsJsonObject();
			
			JsonElement jid = jRep.get("reputation");
			if(jid == null || !jid.isJsonPrimitive()) continue;
			
			String repId;
			if(jid.getAsJsonPrimitive().isString())
            {
                repId = JsonHelper.GetString(jRep, "reputation", "");
            } else
            {
                repId = JsonHelper.GetNumber(jRep, "reputation", 0).toString();
            }
            
			HQMRep repObj = HQMQuestImporter.INSTANCE.reputations.get(repId);
			if(repObj == null) continue;
			
			RewardScoreboard reward = new RewardScoreboard();
			reward.score = repObj.rName;
			reward.value = JsonHelper.GetNumber(jRep, "value", 1).intValue();
			rList.add(reward);
		}
		
		return rList.toArray(new IReward[0]);
	}
}
