package betterquesting.client.gui.tasks;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityList;
import net.minecraft.util.MathHelper;
import org.lwjgl.opengl.GL11;
import betterquesting.client.gui.GuiQuesting;
import betterquesting.client.gui.misc.GuiEmbedded;
import betterquesting.client.themes.ThemeRegistry;
import betterquesting.quests.tasks.TaskHunt;
import betterquesting.utils.RenderUtils;

public class GuiTaskHunt extends GuiEmbedded
{
	TaskHunt task;
	
	public GuiTaskHunt(TaskHunt task, GuiQuesting screen, int posX, int posY, int sizeX, int sizeY)
	{
		super(screen, posX, posY, sizeX, sizeY);
		this.task = task;
	}

	@Override
	public void drawTask(int mx, int my, float partialTick)
	{
		Integer progress = task.userProgress.get(screen.mc.thePlayer.getUniqueID());
		progress = progress == null? 0 : progress;
		String txt = "Kill " + task.idName + " " + progress + "/" + task.required;
		screen.mc.fontRenderer.drawString(txt, posX + sizeX/2 - screen.mc.fontRenderer.getStringWidth(txt)/2, posY, ThemeRegistry.curTheme().textColor().getRGB());
		if(task.target != null)
		{
			GL11.glPushMatrix();
			
			GL11.glColor4f(1F, 1F, 1F, 1F);
			
			float angle = ((float)Minecraft.getSystemTime()%30000F)/30000F * 360F;
			float scale = 64F;
			
			if(task.target.height * scale > (sizeY - 48))
			{
				scale = (sizeY - 48)/task.target.height;
			}
			
			if(task.target.width * scale > sizeX)
			{
				scale = sizeX/task.target.width;
			}
			
			try
			{
				RenderUtils.RenderEntity(posX + sizeX/2, posY + sizeY/2 + MathHelper.ceiling_float_int(task.target.height/2F*scale) + 8, (int)scale, angle, 0F, task.target);
			} catch(Exception e)
			{
			}
			
			GL11.glPopMatrix();
		} else
		{
			if(EntityList.stringToClassMapping.containsKey(task.idName))
			{
				task.target = EntityList.createEntityByName(task.idName, screen.mc.theWorld);
			}
		}
	}
	
}
