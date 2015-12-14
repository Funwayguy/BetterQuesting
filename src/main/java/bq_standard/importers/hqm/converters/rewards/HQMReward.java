package bq_standard.importers.hqm.converters.rewards;

import java.util.ArrayList;
import betterquesting.quests.rewards.RewardBase;
import com.google.gson.JsonElement;

public abstract class HQMReward
{
	public abstract ArrayList<RewardBase> Convert(JsonElement json);
}
