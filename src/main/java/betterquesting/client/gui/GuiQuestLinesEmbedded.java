package betterquesting.client.gui;

import java.util.ArrayList;
import net.minecraft.client.resources.I18n;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MathHelper;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import betterquesting.client.gui.misc.GuiButtonQuestInstance;
import betterquesting.client.gui.misc.GuiEmbedded;
import betterquesting.client.gui.misc.QuestLineButtonTree;
import betterquesting.client.themes.ThemeRegistry;
import betterquesting.client.toolbox.ToolboxTool;
import betterquesting.network.PacketAssembly;
import betterquesting.network.PacketTypeRegistry.BQPacketType;
import betterquesting.quests.QuestDatabase;
import betterquesting.quests.QuestInstance;
import betterquesting.quests.QuestLine;
import betterquesting.quests.QuestLine.QuestLineEntry;
import betterquesting.utils.NBTConverter;
import com.google.gson.JsonObject;

public class GuiQuestLinesEmbedded extends GuiEmbedded
{
	/**
	 * Graph level of zoom out of 100
	 */
	public int zoom = 100;
	public int scrollX = 0;
	public int scrollY = 0;
	int maxX = 0;
	int maxY = 0;
	boolean noScroll = false;
	ToolboxTool curTool = null;
	QuestLine qLine;
	ArrayList<GuiButtonQuestInstance> qBtns = new ArrayList<GuiButtonQuestInstance>();
	
	public GuiQuestLinesEmbedded(GuiQuesting screen, int posX, int posY, int sizeX, int sizeY)
	{
		super(screen, posX, posY, sizeX, sizeY);
	}
	
	public void setCurrentTool(ToolboxTool tool)
	{
		if(curTool != null)
		{
			curTool.deactivateTool();
		}
		
		if(qLine == null)
		{
			curTool = null;
			return;
		}
		
		curTool = tool;
		
		if(tool != null)
		{
			tool.initTool(this);
		}
	}
	
	public ToolboxTool getCurrentTool()
	{
		return curTool;
	}
	
	public void copySettings(GuiQuestLinesEmbedded old)
	{
		this.zoom = old.zoom;
		this.scrollX = old.scrollX;
		this.scrollY = old.scrollY;
		this.setCurrentTool(old.curTool);
	}

	@Override
	public void drawGui(int mx, int my, float partialTick)
	{
		screen.mc.renderEngine.bindTexture(ThemeRegistry.curTheme().guiTexture());
		GL11.glColor4f(1F, 1F, 1F, 1F);
		
		GL11.glPushMatrix();
		double scaleX = sizeX/128D;
		double scaleY = sizeY/128D;
		GL11.glScaled(scaleX, scaleY, 1F);
		GL11.glTranslated(posX/scaleX, posY/scaleY, 0);
		screen.drawTexturedModalRect(0, 0, 0, 128, 128, 128);
		GL11.glPopMatrix();
		
		QuestInstance qTooltip = null;
		
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
				btnQuest.SetClampingBounds(0, 0, rw, rh);
				btnQuest.SetScrollOffset(scrollX, scrollY);
				btnQuest.drawButton(screen.mc, rmx, rmy);
				
				if(btnQuest.visible && screen.isWithin(rmx, rmy, btnQuest.xPosition + scrollX, btnQuest.yPosition + scrollY, btnQuest.width, btnQuest.height, false) && screen.isWithin(mx, my, posX, posY, sizeX, sizeY, false))
				{
					qTooltip = btnQuest.quest;
				}
			}
			
			GL11.glPopMatrix();
			
			GL11.glPushMatrix();
			float scale = sizeX > 600? 1.5F : 1F;
			GL11.glScalef(scale, scale, scale);
			screen.mc.fontRenderer.drawString(I18n.format(qLine.name), MathHelper.ceiling_float_int((posX + 4)/scale), MathHelper.ceiling_float_int((posY + 4)/scale), ThemeRegistry.curTheme().textColor().getRGB(), false);
			screen.mc.fontRenderer.drawString(zoom + "%", MathHelper.ceiling_float_int((posX + 4)/scale), MathHelper.ceiling_float_int((posY + sizeY - 4 - screen.mc.fontRenderer.FONT_HEIGHT)/scale), ThemeRegistry.curTheme().textColor().getRGB(), false);
			GL11.glPopMatrix();
		}
		
		if(curTool != null)
		{
			if(qLine == null)
			{
				this.setCurrentTool(null);
			} else
			{
				curTool.drawTool(mx, my, partialTick);
			}
		}
		
		if(qTooltip != null && (curTool == null || curTool.showTooltips()))
		{
			if(Keyboard.isKeyDown(42) || Keyboard.isKeyDown(54))
			{
				screen.DrawTooltip(qTooltip.getAdvancedTooltip(screen.mc.thePlayer), mx, my);
			} else
			{
				screen.DrawTooltip(qTooltip.getStandardTooltip(screen.mc.thePlayer), mx, my);
			}
		}
	}
	
	/**
	 * Returns quest button under the mouse
	 */
	public GuiButtonQuestInstance getClickedQuest(int mx, int my)
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
	}
	
	@Override
	public void mouseClick(int mx, int my, int click)
	{
		if(curTool != null && curTool.isInitialised(this))
		{
			curTool.onMouseClick(mx, my, click);
		}
		
		if(!screen.isWithin(mx, my, posX, posY, sizeX, sizeY, false))
		{
			noScroll = true;
			return;
		}
		
		noScroll = curTool != null && !curTool.allowDragging(click);
	}
	
	/**
	 * Snaps everything relative to 0,0 and updates max bounds. Will send offset changes server side if told
	 */
	public void autoAlign(boolean applyEdits)
	{
		boolean set = false;
		int xOff = 0;
		int yOff = 0;
		
		for(GuiButtonQuestInstance b : qBtns)
		{
			if(!set)
			{
				xOff = b.xPosition;
				yOff = b.yPosition;
				set = true;
				continue;
			}
			
			if(b.xPosition < xOff)
			{
				xOff = b.xPosition;
			}
			
			if(b.yPosition < yOff)
			{
				yOff = b.yPosition;
			}
		}
		
		maxX = 0;
		maxY = 0;
		
		for(GuiButtonQuestInstance b : qBtns)
		{
			b.xPosition -= xOff;
			b.yPosition -= yOff;
			
			if(b.xPosition + 24 > maxX)
			{
				maxX = b.xPosition + 24;
			}
			
			if(b.yPosition + 24 > maxY)
			{
				maxY = b.yPosition + 24;
			}
			
			if(applyEdits)
			{
				QuestLineEntry entry = qLine.getEntryByID(b.quest.questID);
				
				if(entry != null)
				{
					entry.posX = b.xPosition;
					entry.posY = b.yPosition;
				}
			}
		}
		
		if(curTool == null || curTool.clampScrolling())
		{
			clampScroll();
		}
		
		if(applyEdits)
		{
			NBTTagCompound tags = new NBTTagCompound();
			tags.setInteger("action", 2);
			JsonObject json = new JsonObject();
			QuestDatabase.writeToJson_Lines(json);
			tags.setTag("Data", NBTConverter.JSONtoNBT_Object(json, new NBTTagCompound()));
			PacketAssembly.SendToServer(BQPacketType.LINE_EDIT.GetLocation(), tags);
		}
	}
	
	@Override
	public void mouseScroll(int mx, int my, int SDX)
	{
        if(SDX != 0 && screen.isWithin(mx, my, posX, posY, sizeX, sizeY, false))
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
	
	public void setZoom(int value)
	{
		zoom = MathHelper.clamp_int(value, 50, 200);
		
		if(curTool == null || curTool.clampScrolling())
		{
			clampScroll();
		}
	}
	
	public void setQuestLine(QuestLineButtonTree tree)
	{
		zoom = 100;
		
		if(tree == null)
		{
			this.qLine = null;
			this.qBtns = new ArrayList<GuiButtonQuestInstance>();
			
			this.setCurrentTool(null);
		} else
		{
			this.qLine = tree.line;
			this.qBtns = tree.buttonTree;
			
			autoAlign(false);
			
			scrollX = Math.abs(sizeX - maxX)/2;
			scrollY = 16;
			
			this.setCurrentTool(this.getCurrentTool()); // Force refresh tool
		}
	}
	
	@Override
	public void handleMouse()
	{
		super.handleMouse();
        
    	if((Mouse.isButtonDown(0) && !noScroll) || Mouse.isButtonDown(2))
    	{
			float zs = zoom/100F;
    		scrollX += (Mouse.getEventDX() * screen.width / screen.mc.displayWidth)/zs;
    		scrollY -= (Mouse.getEventDY() * screen.height / screen.mc.displayHeight)/zs;
    		
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
	
	public QuestLine getQuestLine()
	{
		return qLine;
	}
	
	public ArrayList<GuiButtonQuestInstance> getButtons()
	{
		return qBtns;
	}
}
