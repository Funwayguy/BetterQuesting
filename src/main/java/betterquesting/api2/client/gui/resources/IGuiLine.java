package betterquesting.api2.client.gui.resources;

import betterquesting.api2.client.gui.misc.IGuiRect;

public interface IGuiLine
{
	public void drawLine(IGuiRect start, IGuiRect end, int width, int color, float partialTick);
}
