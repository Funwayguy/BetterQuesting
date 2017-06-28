package adv_director.rw2.api.client.gui;

import org.lwjgl.util.Rectangle;
import org.lwjgl.util.vector.Vector4f;
import adv_director.core.AdvDirector;
import adv_director.rw2.api.client.gui.controls.PanelButton;
import adv_director.rw2.api.client.gui.misc.GuiAlign;
import adv_director.rw2.api.client.gui.misc.GuiPadding;
import adv_director.rw2.api.client.gui.misc.GuiTransform;
import adv_director.rw2.api.client.gui.panels.CanvasTextured;
import adv_director.rw2.api.client.gui.panels.lists.CanvasVList;
import adv_director.rw2.api.client.gui.resources.SlicedTexture;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.ResourceLocation;

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
		
		SlicedTexture tex1 = new SlicedTexture(new ResourceLocation(AdvDirector.MODID, "textures/gui/editor_gui.png"), new Rectangle(0, 0, 48, 48), new GuiPadding(16, 16, 16, 16));
		CanvasTextured cvt1 =  new CanvasTextured(tex1);
		this.addPanel(new GuiTransform(new Vector4f(0.05F, 0.05F, 0.95F, 0.95F), new GuiPadding(0, 0, 0, 0), 0), cvt1);
		
		PanelButton btn1 = new PanelButton(0, "Button 1");
		PanelButton btn2 = new PanelButton(1, "Button 2");
		cvt1.addPanel(new GuiTransform(GuiAlign.BOTTOM_CENTER, new GuiPadding(-100, -16, 0, 0), -1), btn1);
		cvt1.addPanel(new GuiTransform(GuiAlign.BOTTOM_CENTER, new GuiPadding(0, -16, -100, 0), -1), btn2);
		
		CanvasVList cl1 = new CanvasVList();
		cvt1.addPanel(new GuiTransform(GuiAlign.TOP_EDGE, new GuiPadding(16, 16, 16, -16), 0), cl1);

		PanelButton btn3 = new PanelButton(2, "List B 1");
		PanelButton btn4 = new PanelButton(3, "List B 2");
		PanelButton btn5 = new PanelButton(4, "List B 3");
		cl1.addPanel(new GuiTransform(GuiAlign.FULL_BOX, new GuiPadding(0, 0, 0, -16), 0), btn3);
		cl1.addPanel(new GuiTransform(GuiAlign.FULL_BOX, new GuiPadding(0, 0, 0, -16), 0), btn4);
		cl1.addPanel(new GuiTransform(GuiAlign.FULL_BOX, new GuiPadding(0, 0, 0, -16), 0), btn5);
	}
	
	@Override
	public void drawScreen(int mx, int my, float partialTick)
	{
		this.drawDefaultBackground();
		
		super.drawScreen(mx, my, partialTick);
	}
}