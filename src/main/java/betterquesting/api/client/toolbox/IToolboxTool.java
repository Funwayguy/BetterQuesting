package betterquesting.api.client.toolbox;

import betterquesting.api.quests.IQuestLineContainer;

public interface IToolboxTool
{
	public void initTool(IQuestLineContainer questLine);
	public void disableTool();
	
	public void drawTool(int mx, int my, float partialTick);
	
	public void onMouseClick(int mx, int my, int click);
	public void onMouseScroll(int mx, int my, int scroll);
	public void onKeyPressed(char c, int key);
	
	public boolean allowTooltips();
	public boolean allowDragging();
	public boolean allowScrolling();
}
