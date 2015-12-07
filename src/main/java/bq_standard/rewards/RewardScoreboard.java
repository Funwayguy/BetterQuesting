package bq_standard.rewards;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreDummyCriteria;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.Scoreboard;
import betterquesting.client.gui.GuiQuesting;
import betterquesting.client.gui.misc.GuiEmbedded;
import betterquesting.quests.rewards.RewardBase;
import betterquesting.utils.JsonHelper;
import bq_standard.client.gui.rewards.GuiRewardScoreboard;
import com.google.gson.JsonObject;

public class RewardScoreboard extends RewardBase
{
	public String score = "Reputation";
	public int value = 1;
	
	@Override
	public String getUnlocalisedName()
	{
		return "betterquesting.reward.scoreboard";
	}
	
	@Override
	public boolean canClaim(EntityPlayer player, NBTTagCompound choiceData)
	{
		return true;
	}
	
	@Override
	public void Claim(EntityPlayer player, NBTTagCompound choiceData)
	{
		Scoreboard board = player.getWorldScoreboard();
		
		if(board == null)
		{
			return;
		}
		
		ScoreObjective scoreObj = board.getObjective(score);
		
		if(scoreObj == null)
		{
			scoreObj = board.addScoreObjective(score, new ScoreDummyCriteria(score));
		}
		
		if(scoreObj == null || scoreObj.getCriteria().isReadOnly())
		{
			return;
		}
		
		Score s = board.func_96529_a(player.getCommandSenderName(), scoreObj);
		
		s.increseScore(value);
	}
	
	@Override
	public void readFromJson(JsonObject json)
	{
		score = JsonHelper.GetString(json, "score", "Reputation");
		value = JsonHelper.GetNumber(json, "value", 1).intValue();
	}
	
	@Override
	public void writeToJson(JsonObject json)
	{
		json.addProperty("score", score);
		json.addProperty("value", value);
	}

	@Override
	public GuiEmbedded getGui(GuiQuesting screen, int posX, int posY, int sizeX, int sizeY)
	{
		return new GuiRewardScoreboard(this, screen, posX, posY, sizeX, sizeY);
	}
}
