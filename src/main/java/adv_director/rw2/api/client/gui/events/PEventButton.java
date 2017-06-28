package adv_director.rw2.api.client.gui.events;

import adv_director.rw2.api.client.gui.controls.PanelButton;

public class PEventButton implements IPanelEvent
{
	private final PanelButton btn;
	
	public PEventButton(PanelButton btn)
	{
		this.btn = btn;
	}
	
	public PanelButton getButton()
	{
		return this.btn;
	}
}
