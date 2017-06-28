package adv_director.rw2.api.client.gui.panels;

import java.util.List;
import adv_director.rw2.api.client.gui.misc.GuiTransform;
import adv_director.rw2.api.client.gui.misc.PanelEntry;

public interface IGuiCanvas extends IGuiPanel
{
	public PanelEntry addPanel(GuiTransform transform, IGuiPanel panel);
	public boolean removePanel(IGuiPanel panel);
	
	public List<PanelEntry> getAllPanels();
}