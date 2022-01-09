package betterquesting.api2.client.gui.panels.bars;

import betterquesting.api.storage.BQ_Settings;
import betterquesting.api2.client.gui.misc.IGuiRect;
import betterquesting.api2.client.gui.resources.textures.IGuiTexture;
import betterquesting.api2.client.gui.themes.presets.PresetTexture;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.input.Mouse;

import java.util.List;

public class PanelHScrollBar implements IScrollBar
{
	private final IGuiRect transform;
	private boolean enabled = true;
	private boolean active = true;
	
	private IGuiTexture texBack;
	private IGuiTexture[] texHandleState = new IGuiTexture[3];
	
	private float scroll = 0F;
	private float speed = BQ_Settings.scrollMultiplier / 20;
	private int hSize = 16;
	private int inset = 0;
	private boolean isDragging = false;
	
	public PanelHScrollBar(IGuiRect rect)
	{
		this.transform = rect;
		this.setBarTexture(PresetTexture.SCROLL_H_BG.getTexture(), PresetTexture.SCROLL_H_0.getTexture(), PresetTexture.SCROLL_H_1.getTexture(), PresetTexture.SCROLL_H_2.getTexture());
	}
	
	@Override
	public PanelHScrollBar setHandleSize(int size, int inset)
	{
		this.hSize = size;
		this.inset = inset;
		return this;
	}
	
	@Override
	public PanelHScrollBar setBarTexture(IGuiTexture back, IGuiTexture handleDisabled, IGuiTexture handleIdle, IGuiTexture handleHover)
	{
		this.texBack = back;
		this.texHandleState[0] = handleDisabled;
		this.texHandleState[1] = handleIdle;
		this.texHandleState[2] = handleHover;
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
	public void setEnabled(boolean state)
	{
		this.enabled = state;
	}
	
	@Override
	public boolean isEnabled()
	{
		return this.enabled;
	}
	
	@Override
	public void setActive(boolean state)
	{
		this.active = state;
	}
	
	@Override
	public boolean isActive()
	{
		return this.active;
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
		
		if(active && isDragging && (Mouse.isButtonDown(0) || Mouse.isButtonDown(2)))
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
		int state = !active ? 0 : (isDragging || bounds.contains(mx, my) ? 2 : 1);
		IGuiTexture tex = texHandleState[state];
		
		if(tex != null)
		{
			tex.drawTexture(bounds.getX() + sx + inset, bounds.getY() + inset, hSize, bounds.getHeight() - (inset * 2), 0F, partialTick);
		}
		
		GlStateManager.popMatrix();
	}

	@Override
	public boolean onMouseClick(int mx, int my, int click)
	{
		IGuiRect bounds = this.getTransform();
		
		if(!active || !bounds.contains(mx, my))
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
		if(!active || sdx == 0 || !bounds.contains(mx, my))
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
		return null;
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
	
	@Override
    public Float readValueRaw()
    {
        return readValue();
    }
    
    @Override
    public void writeValueRaw(Float value)
    {
        this.scroll = value;
    }
}
