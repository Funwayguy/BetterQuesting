package betterquesting.api.client.gui.quest;

import betterquesting.api.client.gui.IGuiEmbedded;
import betterquesting.api.client.toolbox.IToolboxTool;
import betterquesting.api.quests.IQuestLineContainer;
import betterquesting.api.quests.IQuestLineEntry;

public interface IGuiQuestLine extends IGuiEmbedded
{
	public IToolboxTool getActiveTool();
	public void setActiveTool(IToolboxTool tool);
	
	public IQuestLineContainer getQuestLine();
	public IQuestLineEntry getEntryAt(int x, int y);
	
	public float getZoom();
	public int getScrollX();
	public int getScrollY();
}
