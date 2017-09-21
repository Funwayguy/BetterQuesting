package adv_director.rw2.api.client.gui.panels;

import java.util.List;

public interface IGuiCanvas extends IGuiPanel
{
	public void addPanel(IGuiPanel panel);
	public boolean removePanel(IGuiPanel panel);
	
	public List<IGuiPanel> getAllPanels();
}