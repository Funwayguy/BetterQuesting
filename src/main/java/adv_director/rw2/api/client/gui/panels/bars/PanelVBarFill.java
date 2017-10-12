package adv_director.rw2.api.client.gui.panels.bars;

import java.awt.Color;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.util.Rectangle;
import adv_director.api.utils.RenderUtils;
import adv_director.core.AdvDirector;
import adv_director.rw2.api.client.gui.controls.IValueIO;
import adv_director.rw2.api.client.gui.events.PanelEvent;
import adv_director.rw2.api.client.gui.misc.GuiPadding;
import adv_director.rw2.api.client.gui.misc.GuiRectangle;
import adv_director.rw2.api.client.gui.misc.IGuiRect;
import adv_director.rw2.api.client.gui.resources.IGuiTexture;
import adv_director.rw2.api.client.gui.resources.SlicedTexture;

public class PanelVBarFill implements IBarFill
{
	private static final IGuiTexture DEF_BACK = new SlicedTexture(new ResourceLocation(AdvDirector.MODID, "textures/gui/editor_gui_alt.png"), new Rectangle(64, 32, 8, 16), new GuiPadding(3, 6, 3, 6));
	private static final IGuiTexture DEF_FILL = new SlicedTexture(new ResourceLocation(AdvDirector.MODID, "textures/gui/editor_gui_alt.png"), new Rectangle(72, 32, 8, 16), new GuiPadding(3, 6, 3, 6));
	
	private final IGuiRect transform;
	
	private IGuiTexture texBack = DEF_BACK;
	private IGuiTexture texFill = DEF_FILL;
	private IValueIO<Float> fillDriver;
	private boolean flipBar = false;
	private int clrNorm = Color.WHITE.getRGB();
	private int clrLow = Color.WHITE.getRGB();
	private float clrThreshold = 0.25F;
	private boolean lerpClr = false;
	
	public PanelVBarFill(IGuiRect rect)
	{
		this.transform = rect;
		this.fillDriver = new IValueIO<Float>()
		{
			public Float readValue()
			{
				return 1F;
			}
			
			public void writeValue(Float value){}
		};
	}
	
	@Override
	public PanelVBarFill setFillDriver(IValueIO<Float> driver)
	{
		this.fillDriver = driver;
		return this;
	}
	
	@Override
	public PanelVBarFill setFlipped(boolean flipped)
	{
		this.flipBar = flipped;
		return this;
	}
	
	@Override
	public PanelVBarFill setFillColor(int low, int high, float threshold, boolean lerp)
	{
		this.clrNorm = high;
		this.clrLow = low;
		this.clrThreshold = threshold;
		this.lerpClr = lerp;
		return this;
	}
	
	@Override
	public PanelVBarFill setBarTexture(IGuiTexture back, IGuiTexture front)
	{
		this.texBack = back;
		this.texFill = front;
		return this;
	}
	
	@Override
	public void initPanel()
	{
	}
	
	@Override
	public IGuiRect getTransform()
	{
		return transform;
	}
	
	@Override
	public void drawPanel(int mx, int my, float partialTick)
	{
		IGuiRect bounds = this.getTransform();
		GlStateManager.pushMatrix();
		
		GlStateManager.color(1F, 1F, 1F, 1F);
		
		if(texBack != null)
		{
			texBack.drawTexture(bounds.getX(), bounds.getY(), bounds.getWidth(), bounds.getHeight(), 0F);
		}
		
		float f = MathHelper.clamp_float(fillDriver.readValue(), 0F, 1F);
		
		Minecraft mc = Minecraft.getMinecraft();
		
		if(this.flipBar)
		{
			RenderUtils.startScissor(mc, new GuiRectangle(bounds.getX(), bounds.getY(), bounds.getWidth(), (int)(bounds.getHeight() * f), 0));
		} else
		{
			RenderUtils.startScissor(mc, new GuiRectangle(bounds.getX(), bounds.getY() + (int)(bounds.getHeight() - (bounds.getHeight() * f)), bounds.getWidth(), (int)(bounds.getHeight() * f), 0));
		}
		
		if(this.clrThreshold > 0 && f < this.clrThreshold)
		{
			int tmpC = this.clrLow;
			
			if(lerpClr)
			{
				tmpC = RenderUtils.lerpRGB(clrLow, clrNorm, f / clrThreshold);
			}
			
			int a1 = (tmpC >> 24) & 255;
			int r1 = (tmpC >> 16) & 255;
			int g1 = (tmpC >> 8) & 255;
			int b1 = tmpC & 255;
			GlStateManager.color(r1/255F, g1/255F, b1/255F, a1/255F);
		} else
		{
			int a1 = this.clrNorm >> 24 & 255;
			int r1 = this.clrNorm >> 16 & 255;
			int g1 = this.clrNorm >> 8 & 255;
			int b1 = this.clrNorm & 255;
			GlStateManager.color(r1/255F, g1/255F, b1/255F, a1/255F);
		}
		
		if(texFill != null)
		{
			texFill.drawTexture(bounds.getX(), bounds.getY(), bounds.getWidth(), bounds.getHeight(), 0F);
		}
		
		RenderUtils.endScissor(mc);
		
		GlStateManager.popMatrix();
	}
	
	@Override
	public boolean onMouseClick(int mx, int my, int click)
	{
		return false;
	}
	
	@Override
	public boolean onMouseScroll(int mx, int my, int scroll)
	{
		return false;
	}
	
	@Override
	public void onKeyTyped(char c, int keycode)
	{
	}
	
	@Override
	public void onPanelEvent(PanelEvent event)
	{
	}
	
	@Override
	public List<String> getTooltip(int mx, int my)
	{
		return null;
	}
	
}
