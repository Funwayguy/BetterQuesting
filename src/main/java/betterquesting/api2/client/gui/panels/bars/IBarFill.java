package betterquesting.api2.client.gui.panels.bars;

import betterquesting.api2.client.gui.controls.IValueIO;
import betterquesting.api2.client.gui.panels.IGuiPanel;
import betterquesting.api2.client.gui.resources.textures.IGuiTexture;

public interface IBarFill extends IGuiPanel
{
	public IBarFill setFillDriver(IValueIO<Float> driver);
	public IBarFill setFlipped(boolean flipped);
	public IBarFill setFillColor(int low, int high, float threshold, boolean lerp);
	public IBarFill setBarTexture(IGuiTexture back, IGuiTexture front);
}
