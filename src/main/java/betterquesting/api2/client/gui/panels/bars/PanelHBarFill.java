package betterquesting.api2.client.gui.panels.bars;

import java.awt.Color;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.math.MathHelper;
import betterquesting.api.utils.RenderUtils;
import betterquesting.api2.client.gui.controls.IValueIO;
import betterquesting.api2.client.gui.misc.GuiRectangle;
import betterquesting.api2.client.gui.misc.IGuiRect;
import betterquesting.api2.client.gui.resources.textures.IGuiTexture;
import betterquesting.api2.client.gui.themes.presets.PresetTexture;

public class PanelHBarFill implements IBarFill
{
	private final IGuiRect transform;
	
	private IGuiTexture texBack;
	private IGuiTexture texFill;
	private IValueIO<Float> fillDriver;
	private boolean flipBar = false;
	private int clrNorm = Color.WHITE.getRGB();
	private int clrLow = Color.WHITE.getRGB();
	private float clrThreshold = 0.25F;
	private boolean lerpClr = false;
	
	public PanelHBarFill(IGuiRect rect)
	{
		this.texBack = PresetTexture.METER_H_0.getTexture();
		this.texFill = PresetTexture.METER_H_1.getTexture();
		
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
			texBack.drawTexture(bounds.getX(), bounds.getY(), bounds.getWidth(), bounds.getHeight(), 0F, partialTick);
		}
		
		float f = MathHelper.clamp(fillDriver.readValue(), 0F, 1F);
		
		Minecraft mc = Minecraft.getMinecraft();
		
		if(this.flipBar)
		{
			RenderUtils.startScissor(mc, new GuiRectangle(bounds.getX() + (int)(bounds.getWidth() - (bounds.getWidth() * f)), bounds.getY(), (int)(bounds.getWidth() * f), bounds.getHeight(), 0));
		} else
		{
			RenderUtils.startScissor(mc, new GuiRectangle(bounds.getX(), bounds.getY(), (int)(bounds.getWidth() * f), bounds.getHeight(), 0));
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
			texFill.drawTexture(bounds.getX(), bounds.getY(), bounds.getWidth(), bounds.getHeight(), 0F, partialTick);
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
	public boolean onMouseRelease(int mx, int my, int click)
	{
		return false;
	}
	
	@Override
	public boolean onMouseScroll(int mx, int my, int scroll)
	{
		return false;
	}
	
	@Override
	public boolean onKeyTyped(char c, int keycode)
	{
		return false;
	}
	
	@Override
	public List<String> getTooltip(int mx, int my)
	{
		return null;
	}
	
}
