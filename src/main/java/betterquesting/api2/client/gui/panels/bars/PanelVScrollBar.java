package betterquesting.api2.client.gui.panels.bars;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.input.Mouse;
import betterquesting.api2.client.gui.misc.IGuiRect;
import betterquesting.api2.client.gui.resources.IGuiTexture;
import betterquesting.api2.client.gui.themes.presets.PresetTexture;

public class PanelVScrollBar implements IScrollBar
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
	
	public PanelVScrollBar(IGuiRect rect)
	{
		this.transform = rect;
		this.texBack = PresetTexture.SCROLL_V_0.getTexture();
		this.texHndlIdle = PresetTexture.SCROLL_V_1.getTexture();
		this.texHndlHover = PresetTexture.SCROLL_V_2.getTexture();
	}
	
	@Override
	public PanelVScrollBar setHandleSize(int size, int inset)
	{
		this.hSize = size;
		this.inset = inset;
		return this;
	}
	
	@Override
	public PanelVScrollBar setBarTexture(IGuiTexture back, IGuiTexture handleIdle, IGuiTexture handleHover)
	{
		this.texBack = back;
		this.texHndlIdle = handleIdle;
		this.texHndlHover = handleHover;
		return this;
	}
	
	public PanelVScrollBar setScrollSpeed(float f)
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
			float cy = (float)(my - (bounds.getY() + hSize/2)) / (float)(bounds.getHeight() - hSize);
			this.writeValue(cy);
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

		int sy = MathHelper.floor((bounds.getHeight() - hSize - (inset*2)) * scroll);
		
		if(texHndlHover != null && (isDragging || bounds.contains(mx, my)))
		{
			texHndlHover.drawTexture(bounds.getX() + inset, bounds.getY() + sy + inset, bounds.getWidth() - (inset*2), hSize, 0F, partialTick);
		} else if(texHndlIdle != null)
		{
			texHndlIdle.drawTexture(bounds.getX() + inset, bounds.getY() + sy + inset, bounds.getWidth() - (inset*2), hSize, 0F, partialTick);
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
		
		float dy = sdx * speed;
		
		if((dy < 0F && scroll <= 0F) || (dy > 0F && scroll >= 1F))
		{
			return false;
		} else
		{
			this.writeValue(dy + scroll);
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
