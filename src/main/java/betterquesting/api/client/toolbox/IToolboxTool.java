package betterquesting.api.client.toolbox;

import betterquesting.api.client.gui.misc.IGuiQuestLine;

public interface IToolboxTool
{
	public void initTool(IGuiQuestLine gui);
	public void disableTool();
	
	public void drawTool(int mx, int my, float partialTick);
	
	public void onMouseClick(int mx, int my, int click);
	public void onMouseScroll(int mx, int my, int scroll);
	public void onKeyPressed(char c, int key);
	
	public boolean allowTooltips();
	public boolean allowScrolling(int click);
	public boolean allowZoom();
	
	public boolean clampScrolling();
}
