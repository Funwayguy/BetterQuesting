package betterquesting.client.gui.misc;

import org.lwjgl.input.Mouse;
import betterquesting.client.gui.GuiQuesting;

public abstract class GuiEmbedded
{
	protected GuiQuesting screen;
	protected int posX = 0;
	protected int posY = 0;
	protected int sizeX = 1;
	protected int sizeY = 1;
	
	public GuiEmbedded(GuiQuesting screen, int posX, int posY, int sizeX, int sizeY)
	{
		this.screen = screen;
		this.posX = posX;
		this.posY = posY;
		this.sizeX = sizeX;
		this.sizeY = sizeY;
	}
	
	public abstract void drawGui(int mx, int my, float partialTick);
	
	public void handleMouse()
	{
        int i = Mouse.getEventX() * screen.width / screen.mc.displayWidth;
        int j = screen.height - Mouse.getEventY() * screen.height / screen.mc.displayHeight - 1;
        int k = Mouse.getEventButton();
        int SDX = (int)-Math.signum(Mouse.getEventDWheel());
        
        if (Mouse.getEventButtonState())
        {
        	this.mouseClick(i, j, k);
        }
        
        this.mouseScroll(i, j, SDX);
	}
	
	public void mouseClick(int mx, int my, int button)
	{
		
	}
	
	public void mouseScroll(int mx, int my, int delta)
	{
		
	}
	
	public void keyTyped(char character, int keyCode)
	{
		
	}
}
