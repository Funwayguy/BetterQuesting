package betterquesting.client.gui;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.input.Mouse;
import betterquesting.api.client.gui.GuiElement;
import betterquesting.api.client.gui.QuestLineButtonTree;
import betterquesting.api.client.gui.controls.GuiButtonQuestInstance;
import betterquesting.api.client.gui.misc.IGuiQuestLine;
import betterquesting.api.client.toolbox.IToolboxTool;
import betterquesting.api.properties.NativeProps;
import betterquesting.api.questing.IQuest;
import betterquesting.api.questing.IQuestLine;
import betterquesting.api.utils.RenderUtils;
import betterquesting.api2.client.gui.misc.GuiRectangle;

public class GuiQuestLinesEmbedded extends GuiElement implements IGuiQuestLine
{
	private int posX = 0;
	private int posY = 0;
	private int sizeX = 128;
	private int sizeY = 128;
	private int zoom = 100;
	private int scrollX = 0;
	private int scrollY = 0;
	private int maxX = 0;
	private int maxY = 0;
	private boolean noScroll = false;
	private IToolboxTool curTool = null;
	private IQuestLine qLine;
	private final List<GuiButtonQuestInstance> qBtns = new ArrayList<>();
	private QuestLineButtonTree buttonTree = null;
	
	private ResourceLocation bgImg = null;
	private int bgSize = 1024;
	
	List<String> curTooltip = null;
	
	public GuiQuestLinesEmbedded(int posX, int posY, int sizeX, int sizeY)
	{
		this.posX = posX;
		this.posY = posY;
		this.sizeX = sizeX;
		this.sizeY = sizeY;
	}
	
	@Override
	public void setBackground(ResourceLocation image, int size)
	{
		this.bgImg = image;
		this.bgSize = Math.max(1, size);
		
		if(bgImg != null)
		{
			this.maxX = Math.max(bgSize, maxX);
			this.maxY = Math.max(bgSize, maxY);
		}
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
			tool.initTool(this);
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
		
		if(old.getActiveTool() != null)
		{
			old.getActiveTool().disableTool();
			this.setActiveTool(old.getActiveTool());
		}
	}
	
	@Override
	public void drawBackground(int mx, int my, float partialTick)
	{
		mouseDrag(mx, my);
		
		Minecraft mc = Minecraft.getMinecraft();
		double scaleX = sizeX/128D;
		double scaleY = sizeY/128D;
		float zs = zoom/100F;
		int rmx = getRelativeX(mx);
		int rmy = getRelativeY(my);
		
		mc.renderEngine.bindTexture(currentTheme().getGuiTexture());
		GlStateManager.color(1F, 1F, 1F, 1F);
		
		GlStateManager.pushMatrix();
		GlStateManager.scale(scaleX, scaleY, 1F);
		GlStateManager.translate(posX/scaleX, posY/scaleY, 0);
		drawTexturedModalRect(0, 0, 0, 128, 128, 128);
		GlStateManager.popMatrix();
		
		IQuest qTooltip = null;
		
		if(qLine != null)
		{
			GlStateManager.pushMatrix();
			RenderUtils.startScissor(mc, new GuiRectangle(posX, posY, sizeX, sizeY));
			GlStateManager.translate(posX + (scrollX)*zs, posY + (scrollY)*zs, 0);
			GlStateManager.scale(zs, zs, 1F);
			
			if(bgImg != null)
			{
				GlStateManager.pushMatrix();
				GlStateManager.scale(bgSize/256F, bgSize/256F, 1F);
				mc.renderEngine.bindTexture(bgImg);
				this.drawTexturedModalRect(0, 0, 0, 0, 256, 256);
				GlStateManager.popMatrix();
			}
			
			for(int i = 0; i < qBtns.size(); i++)
			{
				GuiButtonQuestInstance btnQuest = qBtns.get(i);
				btnQuest.drawButton(mc, rmx, rmy, partialTick);
				
				if(btnQuest.visible && isWithin(rmx, rmy, btnQuest.x, btnQuest.y, btnQuest.width, btnQuest.height) && isWithin(mx, my, posX, posY, sizeX, sizeY))
				{
					qTooltip = btnQuest.getQuest();
				}
			}
			
			RenderUtils.endScissor();
			GlStateManager.popMatrix();
		}
		
		if(curTool != null)
		{
			GlStateManager.pushMatrix();
			RenderUtils.startScissor(mc, new GuiRectangle(posX, posY, sizeX, sizeY));
			GlStateManager.translate(posX + (scrollX)*zs, posY + (scrollY)*zs, 0);
			GlStateManager.scale(zs, zs, 1F);
			curTool.drawTool(rmx, rmy, partialTick);
			RenderUtils.endScissor();
			GlStateManager.popMatrix();
		}
		
		if(qTooltip != null && (curTool == null || curTool.allowTooltips()))
		{
			curTooltip = qTooltip.getTooltip(mc.player);
		} else
		{
			curTooltip = null;
		}
	}
	
	@Override
	public void drawForeground(int mx, int my, float partialTick)
	{
		Minecraft mc = Minecraft.getMinecraft();
		
		if(qLine != null)
		{
			GlStateManager.pushMatrix();
			float scale = sizeX > 600? 1.5F : 1F;
			GlStateManager.scale(scale, scale, scale);
			drawString(mc.fontRenderer, I18n.format(qLine.getUnlocalisedName()), MathHelper.ceil((posX + 4)/scale), MathHelper.ceil((posY + 4)/scale), getTextColor(), false);
			drawString(mc.fontRenderer, zoom + "%", MathHelper.ceil((posX + 4)/scale), MathHelper.ceil((posY + sizeY - 4 - mc.fontRenderer.FONT_HEIGHT)/scale), getTextColor(), false);
			GlStateManager.popMatrix();
		}
		
		if(curTooltip != null && curTooltip.size() > 0)
		{
			drawTooltip(curTooltip, mx, my, Minecraft.getMinecraft().fontRenderer);
			GlStateManager.disableLighting();
		}
	}
	
	@Override
	public void onMouseClick(int mx, int my, int click)
	{
		if(!isWithin(mx, my, posX, posY, sizeX, sizeY))
		{
			noScroll = true;
			return;
		}
		
		if(curTool != null)
		{
			int rmx = getRelativeX(mx);
			int rmy = getRelativeY(my);
			curTool.onMouseClick(rmx, rmy, click);
		}
		
		noScroll = curTool != null && !curTool.allowScrolling(click);
	}
	
	@Override
	public void onMouseScroll(int mx, int my, int SDX)
	{
        if(SDX != 0 && isWithin(mx, my, posX, posY, sizeX, sizeY))
        {
            if(curTool != null)
            {
            	curTool.onMouseScroll(getRelativeX(mx), getRelativeY(my), SDX);
            	
            	if(!curTool.allowZoom())
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
	
	private void setZoom(int value)
	{
		zoom = MathHelper.clamp(value, 50, 200);
		
		if(curTool == null || !curTool.allowZoom())
		{
			clampScroll();
		}
	}
	
	@Override
	public int getZoom()
	{
		return zoom;
	}
	
	@Override
	public void setQuestLine(QuestLineButtonTree tree, boolean resetView)
	{
		buttonTree = tree;
		
		if(tree == null)
		{
			this.qLine = null;
			this.qBtns.clear();
			this.setBackground(null, 256);
		} else
		{
			this.qLine = tree.getQuestLine();
			this.qBtns.clear();
			this.qBtns.addAll(tree.getButtonTree());

			String bgn = tree.getQuestLine().getProperties().getProperty(NativeProps.BG_IMAGE, "");
			int bgs = tree.getQuestLine().getProperties().getProperty(NativeProps.BG_SIZE, 256).intValue();
			
			if(bgn.length() > 0)
			{
				setBackground(new ResourceLocation(bgn), bgs);
			}
			
			if(bgImg == null)
			{
				maxX = tree.getWidth();
				maxY = tree.getHeight();
			} else
			{
				maxX = Math.max(bgSize, tree.getWidth());
				maxY = Math.max(bgSize, tree.getHeight());
			}
			
			if(resetView)
			{
				zoom = 100;
				scrollX = Math.abs(sizeX - maxX)/2;
				scrollY = 16;
			}
		}
	}
	
	private int lastMX = 0;
	private int lastMY = 0;
	
	private void mouseDrag(int mx, int my)
	{
		int mdx = 0;
		int mdy = 0;
		
		if(lastMX != 0 || lastMY != 0)
		{
			mdx = mx - lastMX;
			mdy = my - lastMY;
		}
		
		lastMX = mx;
		lastMY = my;
		
    	if((Mouse.isButtonDown(0) && !noScroll) || Mouse.isButtonDown(2))
    	{
			float zs = zoom/100F;
    		scrollX += mdx/zs;
    		scrollY += mdy/zs;
    		
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
		int zmx = Math.abs(sx2/2 - (maxX + 32)/2);
		int zmy = Math.abs(sy2/2 - (maxY + 32)/2);
		int zox = sx2/2 - (maxX + 32)/2 + 16;
		int zoy = sy2/2 - (maxY + 32)/2 + 16;
		scrollX = MathHelper.clamp(scrollX, -zmx + zox, zmx + zox);
		scrollY = MathHelper.clamp(scrollY, -zmy + zoy, zmy + zoy);
	}
	
	// Methods below are to assist with editing tools
	
	/**
	 * Convert normal coordinates to canvas coordinates
	 */
	@Override
	public int getRelativeX(int x)
	{
		float zs = zoom/100F;
		return (int)((x - posX)/zs) - scrollX;
	}
	
	/**
	 * Convert normal coordinates to canvas coordinates
	 */
	@Override
	public int getRelativeY(int y)
	{
		float zs = zoom/100F;
		return (int)((y - posY)/zs) - scrollY;
	}
	
	/**
	 * Convert canvas coordinates to normal coordinates
	 */
	@Override
	public int getScreenX(int x)
	{
		float zs = zoom/100F;
		return (int)((x + scrollX)*zs) + posX;
	}
	
	/**
	 * Convert canvas coordinates to normal coordinates
	 */
	@Override
	public int getScreenY(int y)
	{
		float zs = zoom/100F;
		return (int)((y + scrollY)*zs) + posY;
	}
	
	@Override
	public int getPosX()
	{
		return posX;
	}
	
	@Override
	public int getPosY()
	{
		return posY;
	}
	
	@Override
	public int getWidth()
	{
		return sizeX;
	}
	
	@Override
	public int getHeight()
	{
		return sizeY;
	}
	
	@Override
	public int getScrollX()
	{
		return scrollX;
	}
	
	@Override
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
