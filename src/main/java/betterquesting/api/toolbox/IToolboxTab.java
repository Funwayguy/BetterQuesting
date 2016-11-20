package betterquesting.api.toolbox;

import betterquesting.api.client.gui.IGuiEmbedded;
import betterquesting.api.client.gui.IGuiQuestLine;

public interface IToolboxTab
{
	public String getUnlocalisedName();
	
	public void initTools(IGuiQuestLine gui);
	
	public IGuiEmbedded getTabGui(int x, int y, int w, int h);
}
