package adv_director.rw2.api.client.gui.misc;

import adv_director.rw2.api.client.gui.panels.IGuiPanel;

public class PanelEntry implements Comparable<PanelEntry>
{
	private final GuiTransform transform;
	private final IGuiPanel panel;
	
	public PanelEntry(GuiTransform transform, IGuiPanel panel)
	{
		this.transform = transform;
		this.panel = panel;
	}
	
	public GuiTransform getTransform()
	{
		return this.transform;
	}
	
	public IGuiPanel getPanel()
	{
		return this.panel;
	}
	
	@Override
	public int compareTo(PanelEntry entry)
	{
		return this.transform.compareTo(entry.getTransform());
	}
}
