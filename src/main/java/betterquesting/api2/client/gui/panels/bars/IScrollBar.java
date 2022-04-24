package betterquesting.api2.client.gui.panels.bars;

import betterquesting.api2.client.gui.controls.IValueIO;
import betterquesting.api2.client.gui.panels.IGuiPanel;
import betterquesting.api2.client.gui.resources.textures.IGuiTexture;

public interface IScrollBar extends IValueIO<Float>, IGuiPanel {
    void setActive(boolean state);

    boolean isActive();

    IScrollBar setHandleSize(int size, int inset);

    IScrollBar setBarTexture(IGuiTexture background, IGuiTexture handleDisabled, IGuiTexture handleIdle, IGuiTexture handleHover);

    IScrollBar setScrollSpeed(float spd);
}
