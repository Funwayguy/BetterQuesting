package betterquesting.client.toolbox;

import betterquesting.client.gui.GuiQuestLinesEmbedded;
import betterquesting.client.gui.GuiQuesting;

public abstract class ToolboxTool
{
	public GuiQuesting screen;
	public GuiQuestLinesEmbedded ui;
	
	public ToolboxTool(GuiQuesting screen)
	{
		this.screen = screen;
	}
	
	/**
	 * Initialises the tool and/or updates UI variable (button references will need updating)
	 */
	public void initTool(GuiQuestLinesEmbedded ui)
	{
		this.ui = ui;
	}
	
	/**
	 * Deactivates the tool and should end any ongoing functions
	 */
	public void deactivateTool(){}
	
	public void drawTool(int mx, int my, float partialTick){}
	
	public void onMouseClick(int mx, int my, int click){}
	
	public void onMouseScroll(int mx, int my, int sdx){}
	
	/**
	 * For use with text editing or hot keys
	 */
	public void onKeyTyped(int mx, int my, int key){}
	
	/**
	 * Can be used to hide tooltips when dragging quests around
	 */
	public boolean showTooltips()
	{
		return true;
	}
	
	/**
	 * Does this tool allow the UI to be moved with this mouse click?
	 */
	public boolean allowDragging(int click)
	{
		return true;
	}
	
	/**
	 * Does this tool allow the UI to zoom with the scroll wheel?
	 */
	public boolean allowScrolling()
	{
		return true;
	}
	
	/**
	 * Does this tool use scroll clamping
	 */
	public boolean clampScrolling()
	{
		return true;
	}
	
	/**
	 * Used to double check this tool's status before running additional functions
	 */
	public final boolean isInitialised(GuiQuestLinesEmbedded curUi)
	{
		return this.ui != null && this.ui == curUi;
	}
}
