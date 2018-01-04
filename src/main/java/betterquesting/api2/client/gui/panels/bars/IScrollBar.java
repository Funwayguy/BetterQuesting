package betterquesting.api2.client.gui.panels.bars;

import betterquesting.api2.client.gui.controls.IValueIO;
import betterquesting.api2.client.gui.panels.IGuiPanel;
import betterquesting.api2.client.gui.resources.IGuiTexture;

public interface IScrollBar extends IValueIO<Float>, IGuiPanel
{
	public IScrollBar setHandleSize(int size, int inset);
	public IScrollBar setBarTexture(IGuiTexture back, IGuiTexture handleIdle, IGuiTexture handleHover);
	public IScrollBar setScrollSpeed(float spd);
}
