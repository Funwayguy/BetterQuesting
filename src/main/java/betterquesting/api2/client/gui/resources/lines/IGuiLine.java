package betterquesting.api2.client.gui.resources.lines;

import betterquesting.api2.client.gui.misc.IGuiRect;
import betterquesting.api2.client.gui.resources.colors.IGuiColor;

public interface IGuiLine {
    void drawLine(IGuiRect start, IGuiRect end, int width, IGuiColor color, float partialTick);
}
