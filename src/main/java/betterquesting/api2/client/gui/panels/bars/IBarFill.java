package betterquesting.api2.client.gui.panels.bars;

import betterquesting.api2.client.gui.controls.IValueIO;
import betterquesting.api2.client.gui.panels.IGuiPanel;
import betterquesting.api2.client.gui.resources.colors.IGuiColor;
import betterquesting.api2.client.gui.resources.textures.IGuiTexture;

public interface IBarFill extends IGuiPanel {
    IBarFill setFillDriver(IValueIO<Float> driver);

    IBarFill setFlipped(boolean flipped);

    IBarFill setFillColor(IGuiColor color); // Setup the transitional colour manually if necessary

    IBarFill setBarTexture(IGuiTexture back, IGuiTexture front);
}
