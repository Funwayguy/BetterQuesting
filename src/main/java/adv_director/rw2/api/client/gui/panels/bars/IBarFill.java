package adv_director.rw2.api.client.gui.panels.bars;

import adv_director.rw2.api.client.gui.controls.IValueIO;
import adv_director.rw2.api.client.gui.panels.IGuiPanel;

public interface IBarFill extends IGuiPanel
{
	public IBarFill setFillDriver(IValueIO<Float> driver);
	public IBarFill setFlipped(boolean flipped);
	public IBarFill setFillColor(int low, int high, float threshold, boolean lerp);
}
