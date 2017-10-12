package adv_director.rw2.api.client.gui.panels.bars;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.input.Mouse;
import adv_director.rw2.api.client.gui.events.PanelEvent;
import adv_director.rw2.api.client.gui.misc.IGuiRect;
import adv_director.rw2.api.client.gui.resources.IGuiTexture;
import adv_director.rw2.api.client.gui.themes.TexturePreset;
import adv_director.rw2.api.client.gui.themes.ThemeRegistry;

public class PanelVScrollBar implements IScrollBar
{
	private final IGuiRect transform;
	
	private IGuiTexture texBack;
	private IGuiTexture texHndlIdle;
	private IGuiTexture texHndlHover;
	
	private float scroll = 0F;
	private float speed = 0.1F;
	private int hSize = 16;
	private int inset = 1;
	private boolean isDragging = false;
	
	public PanelVScrollBar(IGuiRect rect)
	{
		this.transform = rect;
		this.texBack = ThemeRegistry.INSTANCE.getTexture(TexturePreset.ITEM_FRAME);
		this.texHndlIdle = ThemeRegistry.INSTANCE.getTexture(TexturePreset.SCROLL_V_1);
		this.texHndlHover = ThemeRegistry.INSTANCE.getTexture(TexturePreset.SCROLL_V_2);
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
			texBack.drawTexture(bounds.getX(), bounds.getY(), bounds.getWidth(), bounds.getHeight(), 0F);
		}

		int sy = MathHelper.floor_float((bounds.getHeight() - hSize - (inset*2)) * scroll);
		
		if(texHndlHover != null && (isDragging || bounds.contains(mx, my)))
		{
			texHndlHover.drawTexture(bounds.getX() + inset, bounds.getY() + sy + inset, bounds.getWidth() - (inset*2), hSize, 0F);
		} else if(texHndlIdle != null)
		{
			texHndlIdle.drawTexture(bounds.getX() + inset, bounds.getY() + sy + inset, bounds.getWidth() - (inset*2), hSize, 0F);
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
		this.scroll = MathHelper.clamp_float(value, 0F, 1F);
	}
}
