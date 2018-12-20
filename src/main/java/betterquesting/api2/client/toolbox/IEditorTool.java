package betterquesting.api2.client.toolbox;

import betterquesting.api.questing.IQuestLine;
import betterquesting.api2.client.gui.controls.IPanelButton;
import betterquesting.api2.client.gui.misc.IGuiRect;
import betterquesting.api2.client.gui.panels.IGuiPanel;
import betterquesting.api2.client.gui.panels.lists.CanvasScrolling;

public interface IEditorTool
{
    void setupButton(IPanelButton button);
    
    void activate(IQuestLine questLine, CanvasScrolling scrolling);
    void deactivate();
    
    // Can be used to intercept interaction events for use or cancelling
    IGuiPanel getOverlay(IGuiRect bounds);
    
    // Future support: tool settings panel
    //IGuiPanel getSettings(IGuiRect bounds);
    
    boolean clampScroll();
}
