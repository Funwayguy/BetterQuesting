package betterquesting.client.toolbox;

import betterquesting.client.gui.editors.GuiQuestLineDesigner;
import betterquesting.client.gui.misc.GuiEmbedded;

public abstract class ToolboxGui extends GuiEmbedded
{
	public GuiQuestLineDesigner designer;
	
	public ToolboxGui(GuiQuestLineDesigner designer, int posX, int posY, int sizeX, int sizeY)
	{
		super(designer, posX, posY, sizeX, sizeY);
		this.designer = designer;
	}
	
	public final void refresh_(GuiQuestLineDesigner designer, int posX, int posY, int sizeX, int sizeY)
	{
		this.designer = designer;
		this.posX = posX;
		this.posY = posY;
		this.sizeX = sizeX;
		this.sizeY = sizeY;
		
		this.refreshGui();
	}
	
	/**
	 * An additional rendering pass for things like tooltips, etc.
	 */
	public void drawOverlays(int mx, int my, float partialTick){}
	
	public abstract void refreshGui();
}
