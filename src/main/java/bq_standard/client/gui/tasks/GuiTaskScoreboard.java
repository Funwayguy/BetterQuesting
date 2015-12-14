package bq_standard.client.gui.tasks;

import java.awt.Color;
import net.minecraft.client.resources.I18n;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.Scoreboard;
import betterquesting.client.gui.GuiQuesting;
import betterquesting.client.gui.misc.GuiEmbedded;
import betterquesting.client.themes.ThemeRegistry;
import bq_standard.tasks.TaskScoreboard;

public class GuiTaskScoreboard extends GuiEmbedded
{
	TaskScoreboard task;
	
	public GuiTaskScoreboard(TaskScoreboard task, GuiQuesting screen, int posX, int posY, int sizeX, int sizeY)
	{
		super(screen, posX, posY, sizeX, sizeY);
		this.task = task;
	}

	@Override
	public void drawGui(int mx, int my, float partialTick)
	{
		screen.mc.fontRenderer.drawString(task.scoreName, posX, posY, Color.BLACK.getRGB(), false);
		
		Scoreboard board = screen.mc.thePlayer.getWorldScoreboard();
		ScoreObjective scoreObj = board == null? null : board.getObjective(task.scoreName);
		Score score = scoreObj == null? null : board.func_96529_a(screen.mc.thePlayer.getCommandSenderName(), scoreObj);
		String value = score == null? "?" : "" + score.getScorePoints();
		
		screen.mc.fontRenderer.drawString(I18n.format("bq_standard.gui.condition", value + " " + task.operation.GetText() + " " + task.target), posX, posY + 12, ThemeRegistry.curTheme().textColor().getRGB(), false);
	}
}
