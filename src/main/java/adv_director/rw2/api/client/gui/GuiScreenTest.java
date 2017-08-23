package adv_director.rw2.api.client.gui;

import java.awt.Color;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.util.vector.Vector4f;
import adv_director.rw2.api.client.gui.controls.IValueIO;
import adv_director.rw2.api.client.gui.controls.PanelButton;
import adv_director.rw2.api.client.gui.misc.GuiAlign;
import adv_director.rw2.api.client.gui.misc.GuiPadding;
import adv_director.rw2.api.client.gui.misc.GuiTransform;
import adv_director.rw2.api.client.gui.panels.CanvasTextured;
import adv_director.rw2.api.client.gui.panels.bars.PanelHBarFill;
import adv_director.rw2.api.client.gui.panels.content.PanelPlayerPortrait;
import adv_director.rw2.api.client.gui.themes.TexturePreset;
import adv_director.rw2.api.client.gui.themes.ThemeRegistry;

public class GuiScreenTest extends GuiScreenCanvas
{
	public GuiScreenTest(GuiScreen parent, GuiTransform transform)
	{
		super(parent, transform);
	}
	
	@Override
	public void initGui()
	{
		super.initGui();
		
		CanvasTextured cvt1 =  new CanvasTextured(ThemeRegistry.INSTANCE.getTexture(TexturePreset.PANEL_MAIN));
		this.addPanel(new GuiTransform(new Vector4f(0.05F, 0.05F, 0.95F, 0.95F), new GuiPadding(0, 0, 0, 0), 0), cvt1);
		
		PanelButton btn1 = new PanelButton(0, "Button 1");
		PanelButton btn2 = new PanelButton(1, "Button 2");
		cvt1.addPanel(new GuiTransform(GuiAlign.BOTTOM_CENTER, new GuiPadding(-100, -16, 0, 0), -1), btn1);
		cvt1.addPanel(new GuiTransform(GuiAlign.BOTTOM_CENTER, new GuiPadding(0, -16, -100, 0), -1), btn2);
		
		PanelHBarFill pfb = new PanelHBarFill();
		pfb.setBarTexture(ThemeRegistry.INSTANCE.getTexture(TexturePreset.METER_H_0), ThemeRegistry.INSTANCE.getTexture(TexturePreset.METER_H_0));
		pfb.setFillColor(Color.RED.getRGB(), Color.GREEN.getRGB(), 0.25F, true);
		cvt1.addPanel(new GuiTransform(GuiAlign.BOTTOM_EDGE, new GuiPadding(0, -32, 0, 0), 0), pfb);
		
		pfb.setFillDriver(new IValueIO<Float>()
		{
			@Override
			public Float readValue()
			{
				double d = Math.sin(Math.toRadians((Minecraft.getSystemTime()%10000L)/10000D * 360D));
				return (float)(d + 1F)/2F;
			}

			@Override
			public void writeValue(Float value)
			{
			}
		});
		
		try
		{
			PanelPlayerPortrait pp1 = new PanelPlayerPortrait(UUID.fromString("10755ea6-9721-467a-8b5c-92adf689072c"), "Darkosto");
			PanelPlayerPortrait pp2 = new PanelPlayerPortrait(UUID.fromString("ef35a72a-ef00-4c2a-a2a9-58a54a7bb9fd"), "GreatOrator");
			PanelPlayerPortrait pp3 = new PanelPlayerPortrait(UUID.fromString("4412cc00-65de-43ff-b19a-10e0ec64cc4a"), "Funwayguy");
			cvt1.addPanel(new GuiTransform(GuiAlign.TOP_LEFT, new GuiPadding(0, 0, -64, -64), 0), pp1);
			cvt1.addPanel(new GuiTransform(GuiAlign.TOP_LEFT, new GuiPadding(64, 0, -112, -48), 0), pp2);
			cvt1.addPanel(new GuiTransform(GuiAlign.TOP_LEFT, new GuiPadding(112, 0, -144, -32), 0), pp3);
		} catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	@Override
	public void drawScreen(int mx, int my, float partialTick)
	{
		this.drawDefaultBackground();
		
		super.drawScreen(mx, my, partialTick);
	}
}