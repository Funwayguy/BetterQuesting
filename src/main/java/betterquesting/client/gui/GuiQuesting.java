package betterquesting.client.gui;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiYesNoCallback;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.EnumChatFormatting;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;
import scala.actors.threadpool.Arrays;
import betterquesting.client.gui.misc.GuiButtonQuesting;
import betterquesting.client.gui.misc.GuiEmbedded;
import betterquesting.client.gui.misc.GuiYesNoLocked;
import betterquesting.client.gui.misc.IVolatileScreen;
import betterquesting.client.themes.ThemeRegistry;
import betterquesting.party.PartyManager;
import betterquesting.quests.QuestDatabase;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiQuesting extends GuiScreen implements GuiYesNoCallback
{
	public static final String numRegex = "[^\\.0123456789-]"; // I keep screwing this up so now it's reusable
	
	protected ArrayList<GuiEmbedded> embedded = new ArrayList<GuiEmbedded>();
	protected GuiScreen parent;
	protected String title = "Better Questing";
	
	public int guiLeft = 0;
	public int guiTop = 0;
	public int sizeX = 0;
	public int sizeY = 0;
	
	int mxX = -1;
	int mxY = -1;
	
	public GuiQuesting(GuiScreen parent, String title)
	{
		this.mc = Minecraft.getMinecraft();
		this.fontRendererObj = this.mc.fontRenderer;
		this.parent = parent;
		this.title = title;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void initGui()
	{
		super.initGui();
		
		Keyboard.enableRepeatEvents(true);
		
		QuestDatabase.updateUI = false;
		PartyManager.updateUI = false;
		
		int border = 8; // The minimum distance between the UI and the window edge
		this.sizeX = this.width - border * 2;
		this.sizeY = this.height - border * 2;
		
		this.sizeX = mxX <= 0? sizeX : Math.min(mxX, sizeX);
		this.sizeY = mxY <= 0? sizeY : Math.min(mxY, sizeY);
		
		this.sizeX = this.sizeX - (this.sizeX%16);
		this.sizeY = this.sizeY - (this.sizeY%16);
		
		this.guiLeft = (this.width - this.sizeX)/2;
		this.guiTop = (this.height - this.sizeY)/2;
		
		this.buttonList.clear();
        this.buttonList.add(new GuiButtonQuesting(0, this.guiLeft + this.sizeX/2 - 100, this.guiTop + this.sizeY - 16, I18n.format("gui.done")));
	}
	
	/**
	 * Sets the maximum size of the tiled UI. Use negatives or 0 to disable
	 */
	public void setMaxSize(int maxX, int maxY)
	{
		this.mxX = maxX;
		this.mxY = maxY;
	}
	
	@Override
	public void actionPerformed(GuiButton button)
	{
		if(button.id == 0)
		{
			this.mc.displayGuiScreen(parent);
		}
	}
	
	@Override
    public void onGuiClosed()
    {
    	Keyboard.enableRepeatEvents(false);
    }
	
	@Override
	public void drawScreen(int mx, int my, float partialTick)
	{
		this.drawDefaultBackground();
		
		this.mc.renderEngine.bindTexture(ThemeRegistry.curTheme().guiTexture());
		
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
		this.fontRendererObj.drawString(EnumChatFormatting.BOLD + tmp, this.guiLeft + (sizeX/2) - this.fontRendererObj.getStringWidth(tmp)/2, this.guiTop + 18, ThemeRegistry.curTheme().textColor().getRGB(), false);
		
		super.drawScreen(mx, my, partialTick);
		
		this.mc.renderEngine.bindTexture(ThemeRegistry.curTheme().guiTexture());
		GL11.glColor4f(1F, 1F, 1F, 1F);
	}
	
	/**
	 * Must be called manually when the UI is ready for this layer
	 */
	public void drawEmbedded(int mx, int my, float partialTick)
	{
		for(GuiEmbedded gui : embedded)
		{
			gui.drawGui(mx, my, partialTick);
		}
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
	
    /**
     * Returns true if this GUI should pause the game when it is displayed in single-player
     */
	@Override
    public boolean doesGuiPauseGame()
    {
        return false;
    }
	
	/**
	 * Used for rendering tool tips on this screen via external methods
	 */
	public final void DrawTooltip(List<?> list, int x, int y)
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
	public void handleMouseInput()
	{
		super.handleMouseInput();
		
		for(GuiEmbedded gui : embedded)
		{
			gui.handleMouse();
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
		
		for(GuiEmbedded gui : embedded)
		{
			gui.keyTyped(character, keyCode);
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
}
