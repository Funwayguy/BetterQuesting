package betterquesting.client.gui.tasks;

import java.awt.Color;
import org.lwjgl.opengl.GL11;
import betterquesting.client.gui.GuiQuesting;
import betterquesting.client.gui.misc.GuiEmbedded;
import betterquesting.client.themes.ThemeRegistry;
import betterquesting.quests.tasks.TaskCrafting;
import betterquesting.utils.BigItemStack;
import betterquesting.utils.RenderUtils;
import com.mojang.realmsclient.gui.ChatFormatting;

public class GuiTaskCrafting extends GuiEmbedded
{
	TaskCrafting task;
	
	public GuiTaskCrafting(TaskCrafting task, GuiQuesting screen, int posX, int posY, int sizeX, int sizeY)
	{
		super(screen, posX, posY, sizeX, sizeY);
		this.task = task;
	}

	@Override
	public void drawTask(int mx, int my, float partialTick)
	{
		BigItemStack ttStack = null;
		
		int[] progress = task.userProgress.get(screen.mc.thePlayer.getUniqueID());
		progress = progress == null? new int[task.requiredItems.size()] : progress;
		
		for(int i = 0; i < task.requiredItems.size(); i++)
		{
			BigItemStack stack = task.requiredItems.get(i);
			screen.mc.renderEngine.bindTexture(ThemeRegistry.curTheme().guiTexture());
			GL11.glColor4f(1F, 1F, 1F, 1F);
			GL11.glDisable(GL11.GL_DEPTH_TEST);
			screen.drawTexturedModalRect(posX + (i * 18), posY, 0, 48, 18, 18);
			GL11.glEnable(GL11.GL_DEPTH_TEST);
			int count = stack.stackSize - progress[i];
			
			RenderUtils.RenderItemStack(screen.mc, stack.getBaseStack(), posX + (i * 18) + 1, posY + 1, stack != null && stack.stackSize > 1? "" + count : "");
			
			if(count <= 0 || task.isComplete(screen.mc.thePlayer))
			{
				GL11.glDisable(GL11.GL_DEPTH_TEST);
				screen.mc.fontRenderer.drawString("\u2714", posX + (i * 18) + 6, posY + 6, Color.BLACK.getRGB(), false);
				screen.mc.fontRenderer.drawString("\u2714", posX + (i * 18) + 5, posY + 5, Color.GREEN.getRGB(), false);
				GL11.glEnable(GL11.GL_DEPTH_TEST);
			}
			
			if(screen.isWithin(mx, my, posX + (i * 18), posY, 16, 16, false))
			{
				ttStack = stack;
			}
		}
		
		if(task.isComplete(screen.mc.thePlayer))
		{
			screen.mc.fontRenderer.drawString(ChatFormatting.BOLD + "COMPLETE", posX, posY + 24, Color.GREEN.getRGB(), false);
		} else
		{
			screen.mc.fontRenderer.drawString(ChatFormatting.BOLD + "INCOMPLETE", posX, posY + 24, Color.RED.getRGB(), false);
		}
		
		if(ttStack != null)
		{
			screen.DrawTooltip(ttStack.getBaseStack().getTooltip(screen.mc.thePlayer, screen.mc.gameSettings.advancedItemTooltips), mx, my);
		}
	}
}
