package betterquesting.api.client.toolbox;

import betterquesting.api.client.gui.misc.IGuiEmbedded;
import betterquesting.api.client.gui.misc.IGuiQuestLine;

public interface IToolboxTab
{
    // TODO: Revise all this for the new panels
    // TODO: Allow additional buttons to be registered to tabs
	public String getUnlocalisedName();
	
	public void initTools(IGuiQuestLine gui);
	
	public IGuiEmbedded getTabGui(int x, int y, int w, int h);
}
