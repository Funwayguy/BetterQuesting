package betterquesting.api2.client.toolbox;

import betterquesting.api2.client.gui.misc.IGuiRect;
import betterquesting.api2.client.gui.panels.IGuiPanel;
import betterquesting.api2.client.gui.panels.lists.CanvasQuestLine;
import betterquesting.client.gui2.editors.designer.PanelToolController;

public interface IToolTab {
    String getUnlocalisedName();

    // TODO: Figure out a reasonable way of adding tools
    //void registerTool(IToolboxTool tool, ResourceLocation icon);

    IGuiPanel getTabGui(IGuiRect rect, CanvasQuestLine questLine, PanelToolController toolController);
}
