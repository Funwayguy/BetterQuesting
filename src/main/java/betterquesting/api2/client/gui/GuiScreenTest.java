package betterquesting.api2.client.gui;

import betterquesting.api2.client.gui.controls.IValueIO;
import betterquesting.api2.client.gui.controls.io.ValueFuncIO;
import betterquesting.api2.client.gui.misc.GuiAlign;
import betterquesting.api2.client.gui.misc.GuiPadding;
import betterquesting.api2.client.gui.misc.GuiTransform;
import betterquesting.api2.client.gui.panels.CanvasTextured;
import betterquesting.api2.client.gui.panels.bars.PanelHBarFill;
import betterquesting.api2.client.gui.panels.bars.PanelVBarFill;
import betterquesting.api2.client.gui.resources.colors.GuiColorStatic;
import betterquesting.api2.client.gui.resources.colors.GuiColorTransition;
import betterquesting.api2.client.gui.themes.presets.PresetTexture;
import net.minecraft.client.gui.GuiScreen;

public class GuiScreenTest extends GuiScreenCanvas
{
	public GuiScreenTest(GuiScreen parent)
	{
		super(parent);
	}
	
	@Override
	public void initPanel()
	{
		super.initPanel();
		
		CanvasTextured cvt1 =  new CanvasTextured(new GuiTransform(), PresetTexture.PANEL_MAIN.getTexture());
		this.addPanel(cvt1);
        
        PanelHBarFill phf = new PanelHBarFill(new GuiTransform(GuiAlign.TOP_EDGE, new GuiPadding(72, 16, 72, -32), 0));
        phf.setFillColor(new GuiColorTransition(new GuiColorStatic(0xFFFF0000), new GuiColorStatic(0xFF00FF00)).setupBlending(true, 0.25F));
        cvt1.addPanel(phf);
        
        PanelVBarFill pvf = new PanelVBarFill(new GuiTransform(GuiAlign.LEFT_EDGE, new GuiPadding(16, 72, -72, 72), 0));
        pvf.setFillColor(new GuiColorTransition(new GuiColorStatic(0xFFFF0000), new GuiColorStatic(0xFF00FF00)).setupBlending(true, 0.25F));
        cvt1.addPanel(pvf);
        
        IValueIO<Float> fillDriver = new ValueFuncIO<>(() -> (int)Math.abs(System.currentTimeMillis()%5000L - 2500L)/2500F);
        
        GuiColorTransition transColor = new GuiColorTransition(new GuiColorStatic(0xFF00FF00), new GuiColorStatic(0xFFFF0000)).setupBlending(true, 0.25F);
        transColor.setBlendDriver(fillDriver);
        
        phf.setFillColor(transColor);
        pvf.setFillColor(transColor);
        
        phf.setFillDriver(fillDriver);
        pvf.setFillDriver(fillDriver);
	}
	
	@Override
	public void drawPanel(int mx, int my, float partialTick)
	{
		this.drawDefaultBackground();
		
		super.drawPanel(mx, my, partialTick);
	}
}