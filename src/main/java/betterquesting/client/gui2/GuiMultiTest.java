package betterquesting.client.gui2;

import net.minecraft.client.gui.GuiScreen;
import betterquesting.api2.client.gui.GuiScreenCanvas;
import betterquesting.api2.client.gui.misc.GuiAlign;
import betterquesting.api2.client.gui.misc.GuiPadding;
import betterquesting.api2.client.gui.misc.GuiTransform;
import betterquesting.api2.client.gui.panels.CanvasEmpty;
import betterquesting.api2.client.gui.panels.IGuiPanel;

public class GuiMultiTest extends GuiScreenCanvas
{
	private final IGuiPanel p1;
	private final IGuiPanel p2;
	
	public GuiMultiTest(GuiScreen parent, IGuiPanel p1, IGuiPanel p2)
	{
		super(parent);
		
		this.p1 = p1;
		this.p2 = p2;
	}
	
	public void initPanel()
	{
		super.initPanel();
		
		CanvasEmpty lCan = new CanvasEmpty(new GuiTransform(GuiAlign.HALF_LEFT, new GuiPadding(0, 0, 0, 0), 0));
		this.addPanel(lCan);
		lCan.addPanel(p1);
		CanvasEmpty rCan = new CanvasEmpty(new GuiTransform(GuiAlign.HALF_RIGHT, new GuiPadding(0, 0, 0, 0), 0));
		this.addPanel(rCan);
		rCan.addPanel(p2);
		
		if(p1 instanceof GuiScreen)
		{
			((GuiScreen)p1).setWorldAndResolution(this.mc, lCan.getTransform().getWidth(), lCan.getTransform().getHeight());
		}
		
		if(p2 instanceof GuiScreen)
		{
			((GuiScreen)p2).setWorldAndResolution(this.mc, rCan.getTransform().getWidth(), rCan.getTransform().getHeight());
		}
	}
}
