package betterquesting.client.gui3;

import betterquesting.api2.client.gui.GuiScreenCanvas;
import betterquesting.api2.client.gui.misc.GuiAlign;
import betterquesting.api2.client.gui.misc.GuiPadding;
import betterquesting.api2.client.gui.misc.GuiTransform;
import betterquesting.api2.client.gui.panels.CanvasTextured;
import betterquesting.api2.client.gui.themes.presets.PresetTexture;
import net.minecraft.client.gui.GuiScreen;

public class GuiStatus extends GuiScreenCanvas
{
	public GuiStatus(GuiScreen parent)
	{
		super(parent);
	}
	
	@Override
	public void initPanel()
	{
		super.initPanel();
		
		// Background panel
		CanvasTextured bgCan = new CanvasTextured(new GuiTransform(GuiAlign.FULL_BOX, new GuiPadding(0, 0, 0, 0), 0), PresetTexture.PANEL_MAIN.getTexture());
		this.addPanel(bgCan);
    }
}
