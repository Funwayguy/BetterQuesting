package betterquesting.api.client.gui.quest;

import betterquesting.api.client.gui.IGuiEmbedded;
import betterquesting.api.client.gui.QuestLineButtonTree;
import betterquesting.api.client.toolbox.IToolboxTool;

public interface IGuiQuestLine extends IGuiEmbedded
{
	public IToolboxTool getActiveTool();
	public void setActiveTool(IToolboxTool tool);
	
	// Can be used to modify button positions without making permanent changes
	public QuestLineButtonTree getQuestLine();
	public void setQuestLine(QuestLineButtonTree line);
	
	// These are mostly used in copySettings()
	public int getZoom();
	public int getScrollX();
	public int getScrollY();
	
	public void copySettings(IGuiQuestLine gui);
}
