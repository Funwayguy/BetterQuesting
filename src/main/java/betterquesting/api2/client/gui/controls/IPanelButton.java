package betterquesting.api2.client.gui.controls;

import betterquesting.api2.client.gui.panels.IGuiPanel;

public interface IPanelButton extends IGuiPanel
{
    int getButtonID();
    
    boolean isEnabled();
    void setEnabled(boolean enable);
}
