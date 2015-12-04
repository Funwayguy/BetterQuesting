package betterquesting.client.gui.tasks;

import betterquesting.client.gui.GuiQuesting;
import betterquesting.client.gui.misc.GuiEmbedded;
import betterquesting.quests.tasks.TaskBlockBreak;
import betterquesting.utils.BigItemStack;
import betterquesting.utils.RenderUtils;

public class GuiTaskBlock extends GuiEmbedded
{
	TaskBlockBreak task;
	
	public GuiTaskBlock(TaskBlockBreak task, GuiQuesting screen, int posX, int posY, int sizeX, int sizeY)
	{
		super(screen, posX, posY, sizeX, sizeY);
		this.task = task;
	}

	@Override
	public void drawTask(int mx, int my, float partialTick)
	{
		BigItemStack dispStack = new BigItemStack(task.targetBlock, 1, task.targetMeta);
		RenderUtils.RenderItemStack(screen.mc, dispStack.getBaseStack(), posX, posY, dispStack.stackSize > 0? "" : "" + dispStack.stackSize);
	}
}
