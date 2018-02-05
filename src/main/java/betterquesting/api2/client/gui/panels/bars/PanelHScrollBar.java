package betterquesting.api2.client.gui.panels.bars;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.input.Mouse;
import betterquesting.api2.client.gui.misc.IGuiRect;
import betterquesting.api2.client.gui.resources.IGuiTexture;
import betterquesting.api2.client.gui.themes.presets.PresetTexture;

public class PanelHScrollBar implements IScrollBar
{
	private final IGuiRect transform;
	
	private IGuiTexture texBack;
	private IGuiTexture texHndlIdle;
	private IGuiTexture texHndlHover;
	
	private float scroll = 0F;
	private float speed = 0.1F;
	private int hSize = 16;
	private int inset = 0;
	private boolean isDragging = false;
	
	public PanelHScrollBar(IGuiRect rect)
	{
		this.transform = rect;
		this.texBack = PresetTexture.SCROLL_H_0.getTexture();
		this.texHndlIdle = PresetTexture.SCROLL_H_1.getTexture();
		this.texHndlHover = PresetTexture.SCROLL_H_2.getTexture();
	}
	
	@Override
	public PanelHScrollBar setHandleSize(int size, int inset)
	{
		this.hSize = size;
		this.inset = inset;
		return this;
	}
	
	@Override
	public PanelHScrollBar setBarTexture(IGuiTexture back, IGuiTexture handleIdle, IGuiTexture handleHover)
	{
		this.texBack = back;
		this.texHndlIdle = handleIdle;
		this.texHndlHover = handleHover;
		return this;
	}
	
	@Override
	public PanelHScrollBar setScrollSpeed(float f)
	{
		this.speed = f;
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
		if(isDragging && (Mouse.isButtonDown(0) || Mouse.isButtonDown(2)))
		{
			float cx = (float)(mx - (bounds.getX() + hSize/2)) / (float)(bounds.getWidth() - hSize);
			this.writeValue(cx);
		} else if(isDragging)
		{
			this.isDragging = false;
		}
		
		GlStateManager.pushMatrix();
		GlStateManager.color(1F, 1F, 1F, 1F);
		
		if(texBack != null)
		{
			texBack.drawTexture(bounds.getX(), bounds.getY(), bounds.getWidth(), bounds.getHeight(), 0F, partialTick);
		}

		int sx = MathHelper.floor((bounds.getWidth() - hSize - (inset*2)) * scroll);
		
		if(texHndlHover != null && (isDragging || bounds.contains(mx, my)))
		{
			texHndlHover.drawTexture(bounds.getX() + sx + inset, bounds.getY() + inset, hSize, bounds.getHeight() - (inset*2), 0F, partialTick);
		} else if(texHndlIdle != null)
		{
			texHndlIdle.drawTexture(bounds.getX() + sx + inset, bounds.getY() + inset, hSize, bounds.getHeight() - (inset*2), 0F, partialTick);
		}
		
		GlStateManager.popMatrix();
	}

	@Override
	public boolean onMouseClick(int mx, int my, int click)
	{
		IGuiRect bounds = this.getTransform();
		if(!bounds.contains(mx, my))
		{
			return false;
		}
		
		if(click == 0 || click == 2)
		{
			isDragging = true;
			return true;
		}
		
		return false;
	}
	
	@Override
	public boolean onMouseRelease(int mx, int my, int click)
	{
		return false;
	}

	@Override
	public boolean onMouseScroll(int mx, int my, int sdx)
	{
		IGuiRect bounds = this.getTransform();
		if(sdx == 0 || !bounds.contains(mx, my))
		{
			return false;
		}
		
		float dx = sdx * speed;
		
		if((dx < 0 && scroll <= 0F) || (dx > 0 && scroll >= 1))
		{
			return false;
		} else
		{
			this.writeValue(dx + scroll);
			return true;
		}
	}

	@Override
	public boolean onKeyTyped(char c, int keycode)
	{
		return false;
	}

	@Override
	public List<String> getTooltip(int mx, int my)
	{
		return new ArrayList<String>();
	}
	
	@Override
	public Float readValue()
	{
		return this.scroll;
	}
	
	@Override
	public void writeValue(Float value)
	{
		this.scroll = MathHelper.clamp(value, 0F, 1F);
	}
}
