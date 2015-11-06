package betterquesting.quests.tasks;

import java.awt.Color;
import com.google.gson.JsonObject;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.Scoreboard;
import betterquesting.client.gui.GuiQuesting;
import betterquesting.utils.JsonHelper;

public class TaskScoreboard extends TaskBase
{
	String scoreName = "Score";
	int target = 1;
	ScoreOperation operation = ScoreOperation.MORE_OR_EQUAL;
	
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
	@SideOnly(Side.CLIENT)
	public void drawQuestInfo(GuiQuesting screen, int mouseX, int mouseY, int posX, int posY, int sizeX, int sizeY)
	{
		screen.mc.fontRenderer.drawString("Scoreboard: " + scoreName, posX, posY, Color.BLACK.getRGB(), false);
		
		Scoreboard board = screen.mc.thePlayer.getWorldScoreboard();
		ScoreObjective scoreObj = board == null? null : board.getObjective(scoreName);
		Score score = scoreObj == null? null : board.func_96529_a(screen.mc.thePlayer.getCommandSenderName(), scoreObj);
		
		if(score == null)
		{
			screen.mc.fontRenderer.drawString("Value: NULL", posX, posY + 12, Color.BLACK.getRGB(), false);
		} else
		{
			screen.mc.fontRenderer.drawString("Condition: " + score.getScorePoints() + " " + operation.GetText() + " " + target, posX, posY + 12, Color.BLACK.getRGB(), false);
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
}
