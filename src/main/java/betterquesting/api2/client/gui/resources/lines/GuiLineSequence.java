package betterquesting.api2.client.gui.resources.lines;

import betterquesting.api2.client.gui.misc.IGuiRect;
import betterquesting.api2.client.gui.resources.colors.IGuiColor;

public class GuiLineSequence implements IGuiLine {
    public final IGuiLine[] lines;
    private final float interval;

    public GuiLineSequence(float interval, IGuiLine... lines) {
        this.lines = lines;
        this.interval = interval;
    }

    @Override
    public void drawLine(IGuiRect start, IGuiRect end, int width, IGuiColor color, float partialTick) {
        getCurrentLine().drawLine(start, end, width, color, partialTick);
    }

    public IGuiLine getCurrentLine() {
        return lines[(int) Math.floor((System.currentTimeMillis() / 1000D) % (lines.length * interval) / interval)];
    }

    public IGuiLine[] getAllLines() {
        return lines;
    }
}
