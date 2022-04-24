package betterquesting.api2.client.gui.events.types;

import betterquesting.api2.client.gui.controls.IPanelButton;
import betterquesting.api2.client.gui.events.PanelEvent;

// You don't really need to use this for every button on a GUI. Only the ones you wish to expose for expansions
public class PEventButton extends PanelEvent {
    private final IPanelButton btn;

    public PEventButton(IPanelButton btn) {
        this.btn = btn;
    }

    public IPanelButton getButton() {
        return this.btn;
    }

    @Override
    public boolean canCancel() {
        return true;
    }
}
