package betterquesting.api2.client.gui;

import java.util.UUID;
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.util.vector.Vector4f;
import betterquesting.api2.client.gui.controls.PanelButton;
import betterquesting.api2.client.gui.misc.GuiAlign;
import betterquesting.api2.client.gui.misc.GuiPadding;
import betterquesting.api2.client.gui.misc.GuiRectangle;
import betterquesting.api2.client.gui.misc.GuiTransform;
import betterquesting.api2.client.gui.misc.IGuiRect;
import betterquesting.api2.client.gui.panels.CanvasTextured;
import betterquesting.api2.client.gui.panels.bars.PanelVScrollBar;
import betterquesting.api2.client.gui.panels.content.PanelPlayerPortrait;
import betterquesting.api2.client.gui.panels.lists.CanvasScrolling;
import betterquesting.api2.client.gui.themes.presets.PresetTexture;

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
		
		IGuiRect ctt = new GuiTransform(new Vector4f(0.05F, 0.05F, 0.95F, 0.95F), new GuiPadding(0, 0, 0, 0), 0);
		CanvasTextured cvt1 =  new CanvasTextured(ctt, PresetTexture.PANEL_MAIN.getTexture());
		this.addPanel(cvt1);
		CanvasScrolling cs1 = new CanvasScrolling(new GuiTransform(GuiAlign.FULL_BOX, new GuiPadding(0, 0, 12, 0), 0)).setupAdvanceScroll(true, true, 24);
		cvt1.addPanel(cs1);
		PanelVScrollBar pvs = new PanelVScrollBar(new GuiTransform(GuiAlign.RIGHT_EDGE, new GuiPadding(-12, 4, 4, 4), 0));
		cvt1.addPanel(pvs);
		cs1.setScrollDriverY(pvs);
		
		CanvasTextured cvt2 = new CanvasTextured(new GuiRectangle(0, 0, 800, 500, 0), PresetTexture.PANEL_INNER.getTexture());
		cs1.addPanel(cvt2);
		
		PanelButton btn3 = new PanelButton(new GuiTransform(GuiAlign.BOTTOM_RIGHT, new GuiPadding(-100, -20, 0, 0), 0), 1, "Button 2");
		cs1.addPanel(btn3);
		
		IGuiRect btt1 = new GuiTransform(GuiAlign.BOTTOM_CENTER, new GuiPadding(-100, -16, 0, 0), -1);
		IGuiRect btt2 = new GuiTransform(GuiAlign.BOTTOM_CENTER, new GuiPadding(0, -16, -100, 0), -1);
		PanelButton btn1 = new PanelButton(btt1, 0, "Button 1");
		PanelButton btn2 = new PanelButton(btt2, 1, "Button 2");
		cvt1.addPanel(btn1);
		cvt1.addPanel(btn2);
		
		try
		{
			IGuiRect pt1 = new GuiRectangle(0, 0, 64, 64, 0);
			IGuiRect pt2 = new GuiRectangle(64, 0, 48, 48, 0);
			IGuiRect pt3 = new GuiRectangle(112, 0, 32, 32, 0);
			PanelPlayerPortrait pp1 = new PanelPlayerPortrait(pt1, UUID.fromString("10755ea6-9721-467a-8b5c-92adf689072c"), "Darkosto");
			PanelPlayerPortrait pp2 = new PanelPlayerPortrait(pt2, UUID.fromString("ef35a72a-ef00-4c2a-a2a9-58a54a7bb9fd"), "GreatOrator");
			PanelPlayerPortrait pp3 = new PanelPlayerPortrait(pt3, UUID.fromString("4412cc00-65de-43ff-b19a-10e0ec64cc4a"), "Funwayguy");
			cs1.addPanel(pp1);
			cs1.addPanel(pp2);
			cs1.addPanel(pp3);
		} catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	@Override
	public void drawPanel(int mx, int my, float partialTick)
	{
		this.drawDefaultBackground();
		
		super.drawPanel(mx, my, partialTick);
	}
}