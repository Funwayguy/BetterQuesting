package betterquesting.api2.client.toolbox;

import betterquesting.api.client.toolbox.IToolboxTool;
import betterquesting.api2.client.gui.misc.IGuiRect;
import betterquesting.api2.client.gui.panels.IGuiPanel;
import betterquesting.client.gui2.CanvasQuestLine;

public interface IToolTab
{
    String getUnlocalisedName();
    
    void registerTool(IToolboxTool tool);
    
    IGuiPanel getTabGui(IGuiRect rect, CanvasQuestLine questLine);
}
