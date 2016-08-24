package betterquesting.client.gui;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.MathHelper;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import betterquesting.api.client.gui.GuiElement;
import betterquesting.api.client.gui.QuestLineButtonTree;
import betterquesting.api.client.gui.premade.controls.GuiButtonQuestInstance;
import betterquesting.api.client.gui.quest.IGuiQuestLine;
import betterquesting.api.client.toolbox.IToolboxTool;
import betterquesting.api.quests.IQuestContainer;
import betterquesting.api.quests.IQuestLineContainer;

public class GuiQuestLinesEmbedded extends GuiElement implements IGuiQuestLine
{
	private int posX = 0;
	private int posY = 0;
	private int sizeX = 128;
	private int sizeY = 128;
	/**
	 * Graph level of zoom out of 100
	 */
	private int zoom = 100;
	private int scrollX = 0;
	private int scrollY = 0;
	private int maxX = 0;
	private int maxY = 0;
	private boolean noScroll = false;
	private IToolboxTool curTool = null;
	private IQuestLineContainer qLine;
	private List<GuiButtonQuestInstance> qBtns = new ArrayList<GuiButtonQuestInstance>();
	private QuestLineButtonTree buttonTree = null;
	
	public GuiQuestLinesEmbedded(int posX, int posY, int sizeX, int sizeY)
	{
		this.posX = posX;
		this.posY = posY;
		this.sizeX = sizeX;
		this.sizeY = sizeY;
	}
	
	@Override
	public void setActiveTool(IToolboxTool tool)
	{
		if(curTool != null)
		{
			curTool.disableTool();
		}
		
		curTool = tool;
		
		if(tool != null)
		{
			tool.initTool(qLine);
		}
	}
	
	@Override
	public IToolboxTool getActiveTool()
	{
		return curTool;
	}
	
	@Override
	public void copySettings(IGuiQuestLine old)
	{
		this.zoom = old.getZoom();
		this.scrollX = old.getScrollX();
		this.scrollY = old.getScrollY();
		this.setActiveTool(old.getActiveTool());
	}

	@Override
	public void drawBackground(int mx, int my, float partialTick)
	{
		handleMouse();
		
		Minecraft mc = Minecraft.getMinecraft();
		
		mc.renderEngine.bindTexture(currentTheme().getGuiTexture());
		GL11.glColor4f(1F, 1F, 1F, 1F);
		
		GL11.glPushMatrix();
		double scaleX = sizeX/128D;
		double scaleY = sizeY/128D;
		GL11.glScaled(scaleX, scaleY, 1F);
		GL11.glTranslated(posX/scaleX, posY/scaleY, 0);
		drawTexturedModalRect(0, 0, 0, 128, 128, 128);
		GL11.glPopMatrix();
		
		IQuestContainer qTooltip = null;
		
		if(qLine != null)
		{
			GL11.glPushMatrix();
			GL11.glTranslatef(posX, posY, 0);
			float zs = zoom/100F;
			GL11.glScalef(zs, zs, 1F);
			int rw = (int)(sizeX / zs);
			int rh = (int)(sizeY / zs);
			int rmx = (int)((mx - posX)/zs);
			int rmy = (int)((my - posY)/zs);
			
			for(GuiButtonQuestInstance btnQuest : qBtns)
			{
				btnQuest.drawButton(mc, rmx, rmy);
				
				if(btnQuest.visible && isWithin(rmx, rmy, btnQuest.xPosition + scrollX, btnQuest.yPosition + scrollY, btnQuest.width, btnQuest.height) && isWithin(mx, my, posX, posY, sizeX, sizeY))
				{
					qTooltip = btnQuest.getQuest();
				}
			}
			
			GL11.glPopMatrix();
			
			GL11.glPushMatrix();
			float scale = sizeX > 600? 1.5F : 1F;
			GL11.glScalef(scale, scale, scale);
			drawString(mc.fontRenderer, I18n.format(qLine.getUnlocalisedName()), MathHelper.ceiling_float_int((posX + 4)/scale), MathHelper.ceiling_float_int((posY + 4)/scale), getTextColor(), false);
			drawString(mc.fontRenderer, zoom + "%", MathHelper.ceiling_float_int((posX + 4)/scale), MathHelper.ceiling_float_int((posY + sizeY - 4 - mc.fontRenderer.FONT_HEIGHT)/scale), getTextColor(), false);
			GL11.glPopMatrix();
		}
		
		if(curTool != null)
		{
			curTool.drawTool(mx, my, partialTick);
		}
		
		if(qTooltip != null && (curTool == null || curTool.allowTooltips()))
		{
			/*if(Keyboard.isKeyDown(42) || Keyboard.isKeyDown(54))
			{
				drawTooltip(qTooltip.getAdvancedTooltip(mc.thePlayer), mx, my);
			} else
			{
				drawTooltip(qTooltip.getStandardTooltip(mc.thePlayer), mx, my);
			}*/
		}
	}
	
	@Override
	public void drawForeground(int mx, int my, float partialTick)
	{
		
	}
	
	/*
	 * Returns quest button under the mouse
	 */
	/*public GuiButtonQuestInstance getClickedQuest(int mx, int my)
	{
		if(!screen.isWithin(mx, my, posX, posY, sizeX, sizeY, false))
		{
			return null;
		}
		
		float zs = zoom/100F;
		int rmx = (int)((mx - posX)/zs);
		int rmy = (int)((my - posY)/zs);
		
		for(GuiButtonQuestInstance b : qBtns)
		{
			if(b.mousePressed(screen.mc, rmx, rmy))
			{
				return b;
			}
		}
		
		return null;
	}*/
	
	@Override
	public void onMouseClick(int mx, int my, int click)
	{
		if(curTool != null)
		{
			curTool.onMouseClick(mx, my, click);
		}
		
		if(!isWithin(mx, my, 0, 0, sizeX, sizeY))
		{
			noScroll = true;
			return;
		}
		
		noScroll = curTool != null && !curTool.allowDragging(click);
	}
	
	@Override
	public void onMouseScroll(int mx, int my, int SDX)
	{
        if(SDX != 0 && isWithin(mx, my, 0, 0, sizeX, sizeY))
        {
            if(curTool != null)
            {
            	curTool.onMouseScroll(mx, my, SDX);
            	
            	if(!curTool.allowScrolling())
            	{
            		return; // No zoom for you
            	}
            }
            
        	setZoom(zoom - SDX*5);
        }
	}
	
	@Override
	public void onKeyTyped(char c, int key)
	{
		if(curTool != null)
		{
			curTool.onKeyPressed(c, key);
		}
	}
	
	public void setZoom(int value)
	{
		zoom = MathHelper.clamp_int(value, 50, 200);
		
		if(curTool == null || !curTool.allowScrolling())
		{
			clampScroll();
		}
	}
	
	public int getZoom()
	{
		return zoom;
	}
	
	@Override
	public void setQuestLine(QuestLineButtonTree tree)
	{
		zoom = 100;
		buttonTree = tree;
		
		if(tree == null)
		{
			this.qLine = null;
			this.qBtns = new ArrayList<GuiButtonQuestInstance>();
		} else
		{
			this.qLine = tree.getQuestLine();
			this.qBtns = tree.getButtonTree();
			
			//autoAlign(false);
			
			scrollX = Math.abs(sizeX - maxX)/2;
			scrollY = 16;
		}
	}
	
	private void handleMouse()
	{
    	Minecraft mc = Minecraft.getMinecraft();
    	ScaledResolution scaledresolution = new ScaledResolution(mc, mc.displayWidth, mc.displayHeight);
        int sw = scaledresolution.getScaledWidth();
        int sh = scaledresolution.getScaledHeight();
        
    	if((Mouse.isButtonDown(0) && !noScroll) || Mouse.isButtonDown(2))
    	{
			float zs = zoom/100F;
    		scrollX += (Mouse.getEventDX() * sw / mc.displayWidth)/zs;
    		scrollY -= (Mouse.getEventDY() * sh / mc.displayHeight)/zs;
    		
    		if(curTool == null || curTool.clampScrolling())
    		{
    			clampScroll();
    		}
    	}
	}
	
	public void clampScroll()
	{
		float zs = zoom/100F;
		int sx2 = (int)(sizeX/zs);
		int sy2 = (int)(sizeY/zs);
		int zmx = (int)Math.abs(sx2/2 - (maxX + 32)/2);
		int zmy = (int)Math.abs(sy2/2 - (maxY + 32)/2);
		int zox = sx2/2 - (maxX + 32)/2 + 16;
		int zoy = sy2/2 - (maxY + 32)/2 + 16;
		scrollX = MathHelper.clamp_int(scrollX, -zmx + zox, zmx + zox);
		scrollY = MathHelper.clamp_int(scrollY, -zmy + zoy, zmy + zoy);
	}
	
	// Methods below are to assist with editing tools
	
	/**
	 * Convert normal coordinates to canvas coordinates
	 */
	public int getRelativeX(int x)
	{
		float zs = zoom/100F;
		return (int)((x - posX)/zs) - scrollX;
	}
	
	/**
	 * Convert normal coordinates to canvas coordinates
	 */
	public int getRelativeY(int y)
	{
		float zs = zoom/100F;
		return (int)((y - posY)/zs) - scrollY;
	}
	
	/**
	 * Convert canvas coordinates to normal coordinates
	 */
	public int getScreenX(int x)
	{
		float zs = zoom/100F;
		return (int)((x + scrollX)*zs) + posX;
	}
	
	/**
	 * Convert canvas coordinates to normal coordinates
	 */
	public int getScreenY(int y)
	{
		float zs = zoom/100F;
		return (int)((y + scrollY)*zs) + posY;
	}
	
	public int getPosX()
	{
		return posX;
	}
	
	public int getPosY()
	{
		return posY;
	}
	
	public int getWidth()
	{
		return sizeX;
	}
	
	public int getHeight()
	{
		return sizeY;
	}
	
	public int getScrollX()
	{
		return scrollX;
	}
	
	public int getScrollY()
	{
		return scrollY;
	}
	
	@Override
	public QuestLineButtonTree getQuestLine()
	{
		return buttonTree;
	}
}
