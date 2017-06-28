package adv_director.rw2.api.client.gui.panels.bars;

import adv_director.rw2.api.client.gui.controls.IValueIO;
import adv_director.rw2.api.client.gui.panels.IGuiPanel;
import adv_director.rw2.api.client.gui.resources.IGuiTexture;

public interface IScrollBar extends IValueIO<Float>, IGuiPanel
{
	public IScrollBar setHandleSize(int s);
	public IScrollBar setBarTexture(IGuiTexture back, IGuiTexture handle);
	public IScrollBar setScrollSpeed(float f);
}
