package betterquesting.client.gui.tasks;

import java.awt.Color;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.Scoreboard;
import betterquesting.client.gui.GuiQuesting;
import betterquesting.client.gui.misc.GuiEmbedded;
import betterquesting.quests.tasks.TaskScoreboard;

public class GuiTaskScoreboard extends GuiEmbedded
{
	TaskScoreboard task;
	
	public GuiTaskScoreboard(TaskScoreboard task, GuiQuesting screen, int posX, int posY, int sizeX, int sizeY)
	{
		super(screen, posX, posY, sizeX, sizeY);
		this.task = task;
	}

	@Override
	public void drawTask(int mx, int my, float partialTick)
	{
		screen.mc.fontRenderer.drawString("Scoreboard: " + task.scoreName, posX, posY, Color.BLACK.getRGB(), false);
		
		Scoreboard board = screen.mc.thePlayer.getWorldScoreboard();
		ScoreObjective scoreObj = board == null? null : board.getObjective(task.scoreName);
		Score score = scoreObj == null? null : board.func_96529_a(screen.mc.thePlayer.getCommandSenderName(), scoreObj);
		
		if(score == null)
		{
			screen.mc.fontRenderer.drawString("Value: NULL", posX, posY + 12, Color.BLACK.getRGB(), false);
		} else
		{
			screen.mc.fontRenderer.drawString("Condition: " + score.getScorePoints() + " " + task.operation.GetText() + " " + task.target, posX, posY + 12, Color.BLACK.getRGB(), false);
		}
	}
}
