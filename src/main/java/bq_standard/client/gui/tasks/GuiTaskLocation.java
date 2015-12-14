package bq_standard.client.gui.tasks;

import java.awt.Color;
import net.minecraft.client.resources.I18n;
import betterquesting.client.gui.GuiQuesting;
import betterquesting.client.gui.misc.GuiEmbedded;
import betterquesting.client.themes.ThemeRegistry;
import bq_standard.tasks.TaskLocation;

public class GuiTaskLocation extends GuiEmbedded
{
	TaskLocation task;
	
	public GuiTaskLocation(TaskLocation task, GuiQuesting screen, int posX, int posY, int sizeX, int sizeY)
	{
		super(screen, posX, posY, sizeX, sizeY);
		this.task = task;
	}

	@Override
	public void drawGui(int mx, int my, float partialTick)
	{
		int i = 0;

		screen.mc.fontRenderer.drawString(task.name, posX, posY + i, Color.BLACK.getRGB(), false);
		i += 12;
		
		if(!task.hideInfo)
		{
			if(task.range >= 0)
			{
				screen.mc.fontRenderer.drawString(I18n.format("bq_standard.gui.location", "(" + task.x + ", " + task.y + ", " + task.z + ")"), posX, posY + i, ThemeRegistry.curTheme().textColor().getRGB(), false);
				i += 12;
				screen.mc.fontRenderer.drawString(I18n.format("bq_standard.gui.range", task.range), posX, posY + i, ThemeRegistry.curTheme().textColor().getRGB(), false);
				i += 12;
			}
			
			screen.mc.fontRenderer.drawString(I18n.format("bq_standard.gui.dimension", task.dim), posX, posY + i, ThemeRegistry.curTheme().textColor().getRGB(), false);
			i += 12;
		}
		
		if(task.isComplete(screen.mc.thePlayer.getUniqueID()))
		{
			screen.mc.fontRenderer.drawString(I18n.format("bq_standard.gui.found"), posX, posY + i, Color.GREEN.getRGB(), false);
		} else
		{
			screen.mc.fontRenderer.drawString(I18n.format("bq_standard.gui.undiscovered"), posX, posY + i, Color.RED.getRGB(), false);
		}
	}
	
}
