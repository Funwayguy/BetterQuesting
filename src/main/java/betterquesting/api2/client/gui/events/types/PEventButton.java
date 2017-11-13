package betterquesting.api2.client.gui.events.types;

import betterquesting.api2.client.gui.controls.PanelButton;
import betterquesting.api2.client.gui.events.PanelEvent;

public class PEventButton extends PanelEvent
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
	
	@Override
	public boolean canCancel()
	{
		return true;
	}
}
