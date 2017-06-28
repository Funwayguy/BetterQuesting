package adv_director.api.client.toolbox;

import adv_director.api.client.gui.misc.IGuiEmbedded;
import adv_director.api.client.gui.misc.IGuiQuestLine;

public interface IToolboxTab
{
	public String getUnlocalisedName();
	
	public void initTools(IGuiQuestLine gui);
	
	public IGuiEmbedded getTabGui(int x, int y, int w, int h);
}
