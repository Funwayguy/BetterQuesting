package adv_director.rw2.api.client.gui.panels.bars;

import java.awt.Color;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.Rectangle;
import adv_director.api.utils.RenderUtils;
import adv_director.core.AdvDirector;
import adv_director.rw2.api.client.gui.controls.IValueIO;
import adv_director.rw2.api.client.gui.events.PanelEvent;
import adv_director.rw2.api.client.gui.misc.GuiPadding;
import adv_director.rw2.api.client.gui.misc.IGuiRect;
import adv_director.rw2.api.client.gui.resources.IGuiTexture;
import adv_director.rw2.api.client.gui.resources.SlicedTexture;

public class PanelHBarFill implements IBarFill
{
	private static final IGuiTexture DEF_BACK = new SlicedTexture(new ResourceLocation(AdvDirector.MODID, "textures/gui/editor_gui_alt.png"), new Rectangle(48, 32, 16, 8), new GuiPadding(6, 3, 6, 3));
	private static final IGuiTexture DEF_FILL = new SlicedTexture(new ResourceLocation(AdvDirector.MODID, "textures/gui/editor_gui_alt.png"), new Rectangle(48, 40, 16, 8), new GuiPadding(6, 3, 6, 3));
	
	private final IGuiRect transform;
	
	private IGuiTexture texBack = DEF_BACK;
	private IGuiTexture texFill = DEF_FILL;
	private IValueIO<Float> fillDriver;
	private boolean flipBar = false;
	private int clrNorm = Color.WHITE.getRGB();
	private int clrLow = Color.WHITE.getRGB();
	private float clrThreshold = 0.25F;
	private boolean lerpClr = false;
	
	public PanelHBarFill(IGuiRect rect)
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
	public PanelHBarFill setFillDriver(IValueIO<Float> driver)
	{
		this.fillDriver = driver;
		return this;
	}
	
	@Override
	public PanelHBarFill setFlipped(boolean flipped)
	{
		this.flipBar = flipped;
		return this;
	}
	
	@Override
	public PanelHBarFill setFillColor(int low, int high, float threshold, boolean lerp)
	{
		this.clrNorm = high;
		this.clrLow = low;
		this.clrThreshold = threshold;
		this.lerpClr = lerp;
		return this;
	}
	
	@Override
	public PanelHBarFill setBarTexture(IGuiTexture back, IGuiTexture front)
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
		
		GL11.glEnable(GL11.GL_SCISSOR_TEST);
		
		if(this.flipBar)
		{
			RenderUtils.guiScissor(Minecraft.getMinecraft(), bounds.getX() + (int)(bounds.getWidth() - (bounds.getWidth() * f)), bounds.getY(), (int)(bounds.getWidth() * f), bounds.getHeight());
		} else
		{
			RenderUtils.guiScissor(Minecraft.getMinecraft(), bounds.getX(), bounds.getY(), (int)(bounds.getWidth() * f), bounds.getHeight());
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
		
		GL11.glDisable(GL11.GL_SCISSOR_TEST);
		
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
