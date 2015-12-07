package bq_standard.client.gui.tasks;

import java.awt.Color;
import betterquesting.client.gui.GuiQuesting;
import betterquesting.client.gui.misc.GuiEmbedded;
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
				screen.mc.fontRenderer.drawString("Location: " + task.x + "," + task.y + "," + task.z, posX, posY + i, Color.BLACK.getRGB(), false);
				i += 12;
				screen.mc.fontRenderer.drawString("Range: " + task.range, posX, posY + i, Color.BLACK.getRGB(), false);
				i += 12;
			}
			
			screen.mc.fontRenderer.drawString("Dimension: " + task.dim, posX, posY + i, Color.BLACK.getRGB(), false);
			i += 12;
		}
		
		if(task.isComplete(screen.mc.thePlayer))
		{
			screen.mc.fontRenderer.drawString("Found!", posX, posY + i, Color.GREEN.getRGB(), false);
		} else
		{
			screen.mc.fontRenderer.drawString("Undiscovered", posX, posY + i, Color.RED.getRGB(), false);
		}
	}
	
}
