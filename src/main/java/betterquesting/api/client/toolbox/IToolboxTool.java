package betterquesting.api.client.toolbox;

import betterquesting.client.gui2.CanvasQuestLine;

import java.util.List;

public interface IToolboxTool
{
	void initTool(CanvasQuestLine gui);
	void disableTool();
	
	/** Draws within the relative scrolling portion of the canvas */
	void drawCanvas(int mx, int my, float partialTick);
	/** Draws over the top of the canvas without being affected by scrolling */
	void drawOverlay(int mx, int my, float partialTick);
	
	boolean onMouseClick(int mx, int my, int click);
	boolean onMouseRelease(int mx, int my, int click);
	boolean onMouseScroll(int mx, int my, int scroll);
	boolean onKeyPressed(char c, int key);
	List<String> getTooltip(int mx, int my);
	
	boolean clampScrolling();
}
