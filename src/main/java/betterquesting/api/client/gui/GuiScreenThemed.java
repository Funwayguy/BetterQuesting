package betterquesting.api.client.gui;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiYesNoCallback;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.EnumChatFormatting;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import scala.actors.threadpool.Arrays;
import betterquesting.api.api.ApiReference;
import betterquesting.api.api.QuestingAPI;
import betterquesting.api.client.gui.controls.GuiButtonThemed;
import betterquesting.api.client.gui.misc.IGuiEmbedded;
import betterquesting.api.client.gui.misc.IVolatileScreen;
import betterquesting.api.client.themes.ITheme;
import betterquesting.api.placeholders.ThemeDummy;

public class GuiScreenThemed extends GuiScreen implements GuiYesNoCallback
{
	public ArrayList<IGuiEmbedded> embedded = new ArrayList<IGuiEmbedded>();
	public GuiScreen parent;
	private String title = "Untitled";
	
	public int guiLeft = 0;
	public int guiTop = 0;
	public int sizeX = 0;
	public int sizeY = 0;
	
	private int mxX = -1;
	private int mxY = -1;
	
	public GuiScreenThemed(GuiScreen parent, String title)
	{
		this.mc = Minecraft.getMinecraft();
		this.fontRendererObj = this.mc.fontRenderer;
		
		this.parent = parent;
		this.title = title;
	}
	
	public void setTitle(String title)
	{
		this.title = title;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public void initGui()
	{
		super.initGui();
		
		embedded.clear();
		
		int border = 8; // The minimum distance between the UI and the window edge
		this.sizeX = this.width - border * 2;
		this.sizeY = this.height - border * 2;
		
		this.sizeX = mxX <= 0? sizeX : Math.min(mxX, sizeX);
		this.sizeY = mxY <= 0? sizeY : Math.min(mxY, sizeY);
		
		this.sizeX = this.sizeX - (this.sizeX%16);
		this.sizeY = this.sizeY - (this.sizeY%16);
		
		this.guiLeft = (this.width - this.sizeX)/2;
		this.guiTop = (this.height - this.sizeY)/2;
		
		Keyboard.enableRepeatEvents(true);
		
		this.buttonList.clear();
		this.buttonList.add(new GuiButtonThemed(0, guiLeft + sizeX/2 - 100, guiTop + sizeY - 16, 200, 20, I18n.format("gui.done"), true));
	}
	
	public void drawBackPanel(int mx, int my, float partialTick)
	{
		this.drawDefaultBackground();
		
		this.mc.renderEngine.bindTexture(currentTheme().getGuiTexture());
		
		for(int i = 0; i < this.sizeX; i += 16)
		{
			for(int j = 0; j < this.sizeY; j += 16)
			{
				int tx = 16;
				int ty = 16;
				
				if(i == 0)
				{
					tx -= 16;
				} else if(i == this.sizeX - 16)
				{
					tx += 16;
				}
				
				if(j == 0)
				{
					ty -= 16;
				} else if(j == this.sizeY - 16)
				{
					ty += 16;
				}
				
				this.drawTexturedModalRect(i + this.guiLeft, j + this.guiTop, tx, ty, 16, 16);
			}
		}
		
		String tmp = I18n.format(title);
		this.fontRendererObj.drawString(EnumChatFormatting.BOLD + tmp, this.guiLeft + (sizeX/2) - this.fontRendererObj.getStringWidth(tmp)/2, this.guiTop + 18, getTextColor(), false);
		
		GL11.glColor4f(1F, 1F, 1F, 1F);
		
		for(IGuiEmbedded e : embedded)
		{
			GL11.glPushMatrix();
			e.drawBackground(mx, my, partialTick);
			GL11.glPopMatrix();
			
			GL11.glColor4f(1F, 1F, 1F, 1F);
		}
	}
	
	@Override
	public void drawScreen(int mx, int my, float partialTick)
	{
		GL11.glPushMatrix();
		GL11.glColor4f(1F, 1F, 1F, 1F);
		this.drawBackPanel(mx, my, partialTick);
		GL11.glColor4f(1F, 1F, 1F, 1F);
		super.drawScreen(mx, my, partialTick);
		GL11.glPopMatrix();
		
		GL11.glColor4f(1F, 1F, 1F, 1F);
		
		for(IGuiEmbedded e : embedded)
		{
			GL11.glPushMatrix();
			e.drawForeground(mx, my, partialTick);
			GL11.glColor4f(1F, 1F, 1F, 1F);
			GL11.glPopMatrix();
		}
		
		this.mc.renderEngine.bindTexture(currentTheme().getGuiTexture());
		GL11.glColor4f(1F, 1F, 1F, 1F);
	}
	
	@Override
	public void actionPerformed(GuiButton button)
	{
		if(button.id == 0)
		{
			mc.displayGuiScreen(parent);
		}
	}
	
	@Override
    public void onGuiClosed()
    {
    	Keyboard.enableRepeatEvents(false);
    }
	
	@Override
	public void handleMouseInput()
	{
		super.handleMouseInput();
		
        int i = Mouse.getEventX() * width / mc.displayWidth;
        int j = height - Mouse.getEventY() * height / mc.displayHeight - 1;
        int k = Mouse.getEventButton();
        int SDX = (int)-Math.signum(Mouse.getEventDWheel());
        boolean flag = Mouse.getEventButtonState();
        
        for(IGuiEmbedded gui : embedded)
        {
	        if (flag)
	        {
	        	gui.onMouseClick(i, j, k);
	        }
        }
        
        this.mouseScroll(i, j, SDX);
	}
	
	public void mouseScroll(int mx, int my, int scroll)
	{
		for(IGuiEmbedded gui : embedded)
		{
			gui.onMouseScroll(mx, my, scroll);
		}
	}
	
	@Override
	protected void keyTyped(char character, int keyCode)
	{
        if(keyCode == 1)
        {
        	if(this instanceof IVolatileScreen)
        	{
        		this.mc.displayGuiScreen(new GuiYesNoLocked(this, I18n.format("betterquesting.gui.closing_warning"), I18n.format("betterquesting.gui.closing_confirm"), 0));
        	} else
        	{
	            this.mc.displayGuiScreen((GuiScreen)null);
	            this.mc.setIngameFocus();
        	}
        }
		
		for(IGuiEmbedded gui : embedded)
		{
			gui.onKeyTyped(character, keyCode);
		}
	}
	
	@Override
    public void confirmClicked(boolean confirmed, int id)
	{
		if(confirmed && id == 0)
		{
            this.mc.displayGuiScreen((GuiScreen)null);
            this.mc.setIngameFocus();
		} else
		{
			this.mc.displayGuiScreen(this);
		}
	}
	
	/**
	 * Sets the maximum size of the tiled UI. Use negatives or 0 to disable
	 */
	public void setMaxSize(int maxX, int maxY)
	{
		this.mxX = maxX;
		this.mxY = maxY;
	}
	
	/**
	 * Shortcut method for obtaining the current BetterQuesting theme.
	 */
	public ITheme currentTheme()
	{
		if(QuestingAPI.getAPI(ApiReference.THEME_REG) != null)
		{
			return QuestingAPI.getAPI(ApiReference.THEME_REG).getCurrentTheme();
		} else
		{
			return ThemeDummy.INSTANCE;
		}
	}
	
	/**
	 * Returns the current theme color in integer RGB format
	 */
	public int getTextColor()
	{
		return currentTheme().getTextColor();
	}
	
	/**
	 * Used for rendering tool tips on this screen via external methods
	 */
	public void drawTooltip(List<String> list, int x, int y)
	{
		try
		{
			this.drawHoveringText(list, x, y, fontRendererObj);
		} catch(Exception e)
		{
			this.drawHoveringText(Arrays.asList(new String[]{"ERROR: " + e.getClass().getSimpleName()}), x, y, fontRendererObj);
		}
        GL11.glDisable(GL11.GL_LIGHTING); // Normally not enabled on Questing GUI
	}
	
	@Override
    public final void drawCenteredString(FontRenderer font, String text, int x, int y, int color)
    {
        this.drawCenteredString(font, text, x, y, color, true);
    }
	
    public void drawCenteredString(FontRenderer font, String text, int x, int y, int color, boolean shadow)
    {
        font.drawString(text, x - font.getStringWidth(text) / 2, y, color, shadow);
    }
    
    @Override
    public final void drawString(FontRenderer font, String text, int x, int y, int color)
    {
        this.drawString(font, text, x, y, color, true);
    }
    
    public void drawString(FontRenderer font, String text, int x, int y, int color, boolean shadow)
    {
        font.drawString(text, x, y, color, shadow);
    }
    
    public boolean isWithin(int mx, int my, int startX, int startY, int sizeX, int sizeY)
    {
    	return isWithin(mx, my, startX, startY, sizeX, sizeY, true);
    }
    
    public boolean isWithin(int mx, int my, int startX, int startY, int sizeX, int sizeY, boolean relative)
    {
    	if(relative)
    	{
    		return mx - this.guiLeft >= startX && my - this.guiTop >= startY && mx - this.guiLeft < startX + sizeX && my - this.guiTop < startY + sizeY;
    	} else
    	{
    		return mx >= startX && my >= startY && mx < startX + sizeX && my < startY + sizeY;
    	}
    }
    
    @Override
    public boolean doesGuiPauseGame()
    {
        return false; // Minecraft will halt editor packet handling if paused
    }
}
