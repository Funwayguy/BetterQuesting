package betterquesting.client.toolbox;

import betterquesting.api2.client.gui.misc.GuiRectangle;
import betterquesting.api2.client.gui.misc.IGuiRect;
import betterquesting.api2.client.gui.panels.IGuiPanel;
import betterquesting.api2.client.gui.panels.lists.CanvasQuestLine;
import betterquesting.api2.client.gui.resources.colors.IGuiColor;
import betterquesting.api2.client.gui.resources.lines.IGuiLine;
import betterquesting.api2.client.gui.themes.presets.PresetColor;
import betterquesting.api2.client.gui.themes.presets.PresetLine;
import betterquesting.api2.client.toolbox.IToolTab;
import betterquesting.client.gui2.editors.designer.PanelToolController;

public class ToolboxTabMain implements IToolTab {
    public static final ToolboxTabMain INSTANCE = new ToolboxTabMain();

    private int dragSnap = 4;
    private int[] snaps = new int[]{1, 4, 6, 8, 12, 16, 24, 32};

    @Override
    public String getUnlocalisedName() {
        return "betterquesting.toolbox.tab.main";
    }

    @Override
    public IGuiPanel getTabGui(IGuiRect rect, CanvasQuestLine cvQuestLine, PanelToolController toolController) {
        return new PanelTabMain(rect, cvQuestLine, toolController);
    }

    public void toggleSnap() {
        dragSnap = (dragSnap + 1) % snaps.length;
    }

    public int getSnapValue() {
        return snaps[dragSnap % snaps.length];
    }

    public int getSnapIndex() {
        return dragSnap;
    }

    public void drawGrid(CanvasQuestLine ui) {
        if (getSnapValue() <= 1) return;

        float zs = ui.getZoom();
        int snap = getSnapValue();

        float offX = -ui.getScrollX();
        offX = ((offX % snap + snap) % snap) * zs;
        int midX = Math.floorDiv(-ui.getScrollX(), snap);

        float offY = -ui.getScrollY();
        offY = ((offY % snap + snap) % snap) * zs;
        int midY = Math.floorDiv(-ui.getScrollY(), snap);

        int x = ui.getTransform().getX();
        int y = ui.getTransform().getY();
        int width = ui.getTransform().getWidth();
        int height = ui.getTransform().getHeight();
        int divX = (int) Math.ceil((width - offX) / (zs * snap));
        int divY = (int) Math.ceil((height - offY) / (zs * snap));

        IGuiColor gMinor = PresetColor.GRID_MINOR.getColor();
        IGuiColor gMajor = PresetColor.GRID_MAJOR.getColor();
        IGuiLine lMinor = PresetLine.GRID_MINOR.getLine();
        IGuiLine lMajor = PresetLine.GRID_MAJOR.getLine();

        GuiRectangle p1 = new GuiRectangle(0, 0, 0, 0);
        GuiRectangle p2 = new GuiRectangle(0, 0, 0, 0);

        p1.y = y;
        p2.y = y + height;

        for (int i = 0; i < divX; i++) {
            int lx = x + (int) (i * snap * zs + offX);
            p1.x = lx;
            p2.x = lx;
            if (i == midX) {
                lMajor.drawLine(p1, p2, 2, gMajor, 1F);
            } else {
                lMinor.drawLine(p1, p2, 1, gMinor, 1F);
            }
        }

        p1.x = x;
        p2.x = x + width;

        for (int j = 0; j < divY; j++) {
            int ly = y + (int) (j * snap * zs + offY);
            p1.y = ly;
            p2.y = ly;
            if (j == midY) {
                lMajor.drawLine(p1, p2, 2, gMajor, 1F);
            } else {
                lMinor.drawLine(p1, p2, 1, gMinor, 1F);
            }
        }
    }
}
