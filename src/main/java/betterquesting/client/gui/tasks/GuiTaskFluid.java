package betterquesting.client.gui.tasks;

import java.awt.Color;
import java.util.ArrayList;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.MathHelper;
import net.minecraftforge.fluids.FluidStack;
import org.lwjgl.opengl.GL11;
import betterquesting.client.gui.GuiQuesting;
import betterquesting.client.gui.misc.GuiEmbedded;
import betterquesting.client.themes.ThemeRegistry;
import betterquesting.quests.tasks.TaskFluid;
import betterquesting.utils.RenderUtils;
import com.mojang.realmsclient.gui.ChatFormatting;

public class GuiTaskFluid extends GuiEmbedded
{
	TaskFluid task;
	int scroll = 0;
	
	public GuiTaskFluid(TaskFluid task, GuiQuesting screen, int posX, int posY, int sizeX, int sizeY)
	{
		super(screen, posX, posY, sizeX, sizeY);
		this.task = task;
	}

	@Override
	public void drawTask(int mx, int my, float partialTick)
	{
		int rowLMax = (sizeX - 40)/18;
		int rowL = Math.min(task.requiredFluids.size(), rowLMax);
		
		if(rowLMax < task.requiredFluids.size())
		{
			scroll = MathHelper.clamp_int(scroll, 0, task.requiredFluids.size() - rowLMax);
			RenderUtils.DrawFakeButton(screen, posX, posY, 20, 20, "<", screen.isWithin(mx, my, posX, posY, 20, 20, false)? 2 : 1);
			RenderUtils.DrawFakeButton(screen, posX + 20 + 18*rowL, posY, 20, 20, ">", screen.isWithin(mx, my, posX + 20 + 18*rowL, posY, 20, 20, false)? 2 : 1);
		} else
		{
			scroll = 0;
		}
		
		FluidStack ttStack = null;
		String ttAmount = "0/0 mB";
		
		int[] progress = task.userProgress.get(screen.mc.thePlayer.getUniqueID());
		progress = progress == null? new int[task.requiredFluids.size()] : progress;
		
		for(int i = 0; i < rowL; i++)
		{
			FluidStack stack = task.requiredFluids.get(i + scroll);
			screen.mc.renderEngine.bindTexture(ThemeRegistry.curTheme().guiTexture());
			GL11.glColor4f(1F, 1F, 1F, 1F);
			GL11.glDisable(GL11.GL_DEPTH_TEST);
			screen.drawTexturedModalRect(posX + (i * 18) + 20, posY, 0, 48, 18, 18);
			GL11.glEnable(GL11.GL_DEPTH_TEST);
			int count = stack.amount - progress[i + scroll];
			
			if(stack != null)
			{
				screen.mc.renderEngine.bindTexture(TextureMap.locationBlocksTexture);
				if(progress[i] >= stack.amount)
				{
					GL11.glColor4f(1F, 1F, 1F, 1F);
					RenderUtils.itemRender.renderIcon(posX + (i * 18) + 21, posY + 1, stack.getFluid().getIcon(), 16, 16);
				} else
				{
					GL11.glColor4f(1F, 1F, 1F, 0.25F);
					RenderUtils.itemRender.renderIcon(posX + (i * 18) + 21, posY + 1, stack.getFluid().getIcon(), 16, 16);
					GL11.glColor4f(1F, 1F, 1F, 1F);
					RenderUtils.itemRender.renderIcon(posX + (i * 18) + 21, posY + 1 - MathHelper.floor_float(16 * (progress[i]/(float)stack.amount) - 16), stack.getFluid().getIcon(), 16, MathHelper.floor_float(16 * (progress[i]/(float)stack.amount)));
				}
			}
			
			if(count <= 0 || task.isComplete(screen.mc.thePlayer))
			{
				GL11.glDisable(GL11.GL_DEPTH_TEST);
				// Shadows don't work on these symbols for some reason so we manually draw a shadow
				screen.mc.fontRenderer.drawString("\u2714", posX + (i * 18) + 26, posY + 6, Color.BLACK.getRGB(), false);
				screen.mc.fontRenderer.drawString("\u2714", posX + (i * 18) + 25, posY + 5, Color.GREEN.getRGB(), false);
				GL11.glEnable(GL11.GL_DEPTH_TEST);
			}
			
			if(screen.isWithin(mx, my, posX + (i * 18) + 21, posY, 16, 16, false))
			{
				ttStack = stack;
				ttAmount = progress[i] + "/" + stack.amount + " mB";
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
			ArrayList<String> tTip = new ArrayList<String>();
			tTip.add(ttStack.getLocalizedName());
			tTip.add(ChatFormatting.GRAY + ttAmount);
			screen.DrawTooltip(tTip, mx, my);
		}
	}
	
	@Override
	public void mouseClick(int mx, int my, int button)
	{
		if(button != 0)
		{
			return;
		}
		
		int rowLMax = (sizeX - 40)/18;
		int rowL = Math.min(task.requiredFluids.size(), rowLMax);
		
		if(screen.isWithin(mx, my, posX, posY, 20, 20, false))
		{
			scroll = MathHelper.clamp_int(scroll - 1, 0, task.requiredFluids.size() - rowLMax);
		} else if(screen.isWithin(mx, my, posX + 20 + 18*rowL, posY, 20, 20, false))
		{
			scroll = MathHelper.clamp_int(scroll + 1, 0, task.requiredFluids.size() - rowLMax);
		}
	}
}
