package betterquesting.api.client.toolbox;

import betterquesting.api.client.gui.IGuiEmbedded;
import betterquesting.api.client.gui.quest.IGuiQuestLine;

public interface IToolboxTab
{
	public String getUnlocalisedName();
	
	public void initTools(IGuiQuestLine gui);
	
	public IGuiEmbedded getTabGui(int x, int y, int w, int h);
}
