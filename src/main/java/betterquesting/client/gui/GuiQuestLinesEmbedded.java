package betterquesting.client.gui;

import java.util.ArrayList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.MathHelper;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import betterquesting.client.gui.misc.GuiButtonQuestInstance;
import betterquesting.client.gui.misc.GuiButtonQuestLine;
import betterquesting.client.gui.misc.GuiEmbedded;
import betterquesting.client.themes.ThemeRegistry;
import betterquesting.quests.QuestInstance;
import betterquesting.quests.QuestLine;
import betterquesting.quests.tasks.TaskBase;
import com.mojang.realmsclient.gui.ChatFormatting;

public class GuiQuestLinesEmbedded extends GuiEmbedded
{
	/**
	 * Graph level of zoom out of 100
	 */
	int zoom = 100;
	int scrollX = 0;
	int scrollY = 0;
	int maxX = 0;
	int maxY = 0;
	boolean flag = false;
	QuestLine qLine;
	ArrayList<GuiButtonQuestInstance> qBtns = new ArrayList<GuiButtonQuestInstance>();
	
	public GuiQuestLinesEmbedded(GuiQuesting screen, int posX, int posY, int sizeX, int sizeY)
	{
		super(screen, posX, posY, sizeX, sizeY);
	}

	@Override
	public void drawGui(int mx, int my, float partialTick)
	{
		Minecraft mc = Minecraft.getMinecraft();
		
		mc.renderEngine.bindTexture(ThemeRegistry.curTheme().guiTexture());
		
		GL11.glPushMatrix();
		double scaleX = sizeX/128D;
		double scaleY = sizeY/128D;
		GL11.glScaled(scaleX, scaleY, 1F);
		screen.drawTexturedModalRect((int)Math.round((posX)/scaleX), (int)Math.round((posY)/scaleY), 0, 128, 128, 128);
		GL11.glPopMatrix();
		
		QuestInstance qTooltip = null;
		
		if(qLine != null)
		{
			for(GuiButtonQuestInstance btnQuest : qBtns)
			{
				btnQuest.SetScrollOffset(scrollX, scrollY);
				btnQuest.drawButton(mc, mx, my);
				
				if(btnQuest.visible && screen.isWithin(mx, my, btnQuest.xPosition + scrollX, btnQuest.yPosition + scrollY, btnQuest.width, btnQuest.height, false))
				{
					qTooltip = btnQuest.quest;
				}
			}
			
			GL11.glDisable(GL11.GL_DEPTH_TEST);
			
			//RenderUtils.drawSplitString(mc.fontRenderer, I18n.format(qLine.description), posX + 174, posY + 32 + this.sizeY - 64 - 32 + 4, this.sizeX - (32 + 150 + 8), ThemeRegistry.curTheme().textColor().getRGB(), false);
			
			GL11.glPushMatrix();
			float scale = sizeX > 600? 1.5F : 1F;
			GL11.glScalef(scale, scale, scale);
			mc.fontRenderer.drawString(ChatFormatting.BOLD + I18n.format(qLine.name), MathHelper.ceiling_float_int((posX + 4)/scale), MathHelper.ceiling_float_int((posY + 4)/scale), ThemeRegistry.curTheme().textColor().getRGB(), false);
			GL11.glPopMatrix();
		}
		
		if(qTooltip != null)
		{
			ArrayList<String> qInfo = new ArrayList<String>();
			qInfo.add(I18n.format(qTooltip.name));
			if(qTooltip.isComplete(mc.thePlayer.getUniqueID()))
			{
				qInfo.add(ChatFormatting.GREEN + I18n.format("betterquesting.tooltip.complete"));
				
				if(!qTooltip.HasClaimed(mc.thePlayer.getUniqueID()))
				{
					qInfo.add(ChatFormatting.GRAY + I18n.format("betterquesting.tooltip.rewards_pending"));
				}
			} else if(!qTooltip.isUnlocked(mc.thePlayer.getUniqueID()))
			{
				qInfo.add(ChatFormatting.RED + "" + ChatFormatting.UNDERLINE + I18n.format("betterquesting.tooltip.requires"));
				
				for(QuestInstance req : qTooltip.preRequisites)
				{
					if(!req.isComplete(mc.thePlayer.getUniqueID()))
					{
						qInfo.add(ChatFormatting.RED + "- " + I18n.format(req.name));
					}
				}
			} else
			{
				int n = 0;
				
				for(TaskBase task : qTooltip.tasks)
				{
					if(task.isComplete(mc.thePlayer.getUniqueID()))
					{
						n++;
					}
				}
				
				qInfo.add(ChatFormatting.GRAY + I18n.format("betterquesting.tooltip.tasks_complete", n, qTooltip.tasks.size()));
			}
			screen.DrawTooltip(qInfo, mx, my);
		}
	}
	
	@Override
	public void mouseClick(int mx, int my, int button)
	{
		if(!screen.isWithin(mx, my, posX, posY, sizeX, sizeY, false))
		{
			flag = true;
			return;
		}
		
		flag = false;
		
		for(GuiButtonQuestInstance b : qBtns)
		{
			if(b.mousePressed(Minecraft.getMinecraft(), mx, my))
			{
				flag = true;
				break;
			}
		}
	}
	
	@Override
	public void mouseScroll(int mx, int my, int SDX)
	{
        if(SDX != 0 && screen.isWithin(mx, my, posX, posY, sizeX, sizeY, false));
        {
        	zoom = MathHelper.clamp_int(zoom + SDX, 1, 200);
        }
	}
	
	public void setZoom(int value)
	{
		zoom = MathHelper.clamp_int(zoom, 1, 200);
	}
	
	public void setQuestLine(GuiButtonQuestLine qlBtn)
	{
		if(qlBtn == null)
		{
			this.qLine = null;
			this.qBtns = new ArrayList<GuiButtonQuestInstance>();
		} else
		{
			this.qLine = qlBtn.line;
			this.qBtns = qlBtn.buttonTree;
			
			maxX = Math.abs(sizeX/2 - (qlBtn.treeW + 32)/2);
			maxY = Math.abs(sizeY/2 - (qlBtn.treeH + 32)/2);
			scrollX = 0;
			scrollY = maxY;
		}
	}
	
	@Override
	public void handleMouse()
	{
		super.handleMouse();
        
    	if(!flag && (Mouse.isButtonDown(0) || Mouse.isButtonDown(2)))
    	{
    		scrollX += Mouse.getEventDX() * screen.width / screen.mc.displayWidth;
    		scrollY -= Mouse.getEventDY() * screen.height / screen.mc.displayHeight;
    		scrollX = MathHelper.clamp_int(scrollX, -maxX, maxX);
    		scrollY = MathHelper.clamp_int(scrollY, -maxY, maxY);
    	}
	}
}
