package betterquesting.quests.tasks;

import java.util.UUID;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.Scoreboard;
import betterquesting.client.gui.GuiQuesting;
import betterquesting.client.gui.misc.GuiEmbedded;
import betterquesting.client.gui.tasks.GuiTaskScoreboard;
import betterquesting.utils.JsonHelper;
import com.google.gson.JsonObject;

public class TaskScoreboard extends TaskBase
{
	public String scoreName = "Score";
	public int target = 1;
	public ScoreOperation operation = ScoreOperation.MORE_OR_EQUAL;
	
	@Override
	public String getUnlocalisedName()
	{
		return "betterquesting.task.scoreboard";
	}
	
	@Override
	public void Update(EntityPlayer player)
	{
		if(player.ticksExisted%20 == 0) // Auto-detect once per second
		{
			Detect(player);
		}
	}
	
	@Override
	public void Detect(EntityPlayer player)
	{
		if(isComplete(player))
		{
			return;
		}
		
		Scoreboard board = player.getWorldScoreboard();
		ScoreObjective scoreObj = board == null? null : board.getObjective(scoreName);
		
		if(scoreObj == null)
		{
			return;
		}
		
		Score score = board.func_96529_a(player.getCommandSenderName(), scoreObj);
		int points = score.getScorePoints();
		
		boolean flag = false;
		
		switch(operation)
		{
			case EQUAL:
				flag = points == target;
				break;
			case LESS_THAN:
				flag = points < target;
				break;
			case MORE_THAN:
				flag = points > target;
				break;
			case LESS_OR_EQUAL:
				flag = points <= target;
				break;
			case MORE_OR_EQUAL:
				flag = points >= target;
				break;
		}
		
		if(flag)
		{
			this.completeUsers.add(player.getUniqueID());
		}
	}
	
	@Override
	public void writeToJson(JsonObject json)
	{
		json.addProperty("scoreName", scoreName);
		json.addProperty("target", target);
		json.addProperty("operation", operation.name());
		
		super.writeToJson(json);
	}
	
	@Override
	public void readFromJson(JsonObject json)
	{
		scoreName = JsonHelper.GetString(json, "scoreName", "Score");
		target = JsonHelper.GetNumber(json, "target", 1).intValue();
		operation = ScoreOperation.valueOf(JsonHelper.GetString(json, "operation", "MORE_OR_EQUAL").toUpperCase());
		operation = operation != null? operation : ScoreOperation.MORE_OR_EQUAL;
		
		super.readFromJson(json);
	}
	
	public static enum ScoreOperation
	{
		EQUAL("="),
		LESS_THAN("<"),
		MORE_THAN(">"),
		LESS_OR_EQUAL("<="),
		MORE_OR_EQUAL(">=");
		
		String text = "";
		ScoreOperation(String text)
		{
			this.text = text;
		}
		
		public String GetText()
		{
			return text;
		}
	}

	@Override
	public void ResetProgress(UUID uuid)
	{
		completeUsers.remove(uuid);
	}

	@Override
	public void ResetAllProgress()
	{
		completeUsers.clear();
	}

	@Override
	public GuiEmbedded getGui(GuiQuesting screen, int posX, int posY, int sizeX, int sizeY)
	{
		return new GuiTaskScoreboard(this, screen, posX, posY, sizeX, sizeY);
	}
}
