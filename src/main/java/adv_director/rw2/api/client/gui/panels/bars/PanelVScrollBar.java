package adv_director.rw2.api.client.gui.panels.bars;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.input.Mouse;
import org.lwjgl.util.Rectangle;
import adv_director.core.AdvDirector;
import adv_director.rw2.api.client.gui.events.IPanelEvent;
import adv_director.rw2.api.client.gui.misc.GuiPadding;
import adv_director.rw2.api.client.gui.panels.IGuiPanel;
import adv_director.rw2.api.client.gui.resources.IGuiTexture;
import adv_director.rw2.api.client.gui.resources.SlicedTexture;

public class PanelVScrollBar implements IScrollBar
{
	private static final IGuiTexture DEF_BACK = new SlicedTexture(new ResourceLocation(AdvDirector.MODID, "textures/gui/editor_gui_alt.png"), new Rectangle(48, 0, 8, 16), new GuiPadding(1, 1, 1, 1));
	private static final IGuiTexture DEF_HNDL = new SlicedTexture(new ResourceLocation(AdvDirector.MODID, "textures/gui/editor_gui_alt.png"), new Rectangle(56, 0, 8, 16), new GuiPadding(3, 4, 3, 4));
	
	private final Rectangle bounds = new Rectangle(0, 0, 1, 1);
	private IGuiPanel parent;
	private IGuiTexture texBack = DEF_BACK;
	private IGuiTexture texHndl = DEF_HNDL;
	
	private float scroll = 0F;
	private float speed = 0.1F;
	private int hSize = 16;
	private boolean isDragging = false;
	
	@Override
	public PanelVScrollBar setHandleSize(int size)
	{
		this.hSize = size;
		return this;
	}
	
	@Override
	public PanelVScrollBar setBarTexture(IGuiTexture back, IGuiTexture handle)
	{
		this.texBack = back;
		this.texHndl = handle;
		return this;
	}
	
	public PanelVScrollBar setScrollSpeed(float f)
	{
		this.speed = f;
		return this;
	}
	
	@Override
	public IGuiPanel getParentPanel()
	{
		return parent;
	}

	@Override
	public void setParentPanel(IGuiPanel panel)
	{
		this.parent = panel;
	}

	@Override
	public void initPanel()
	{
	}
	
	@Override
	public void updateBounds(Rectangle bounds)
	{
		this.bounds.setBounds(bounds);
	}

	@Override
	public Rectangle getBounds()
	{
		return this.bounds;
	}

	@Override
	public void drawPanel(int mx, int my, float partialTick)
	{
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
		
		texBack.drawTexture(bounds.getX(), bounds.getY(), bounds.getWidth(), bounds.getHeight(), 0F);
		int sy = MathHelper.floor_float((bounds.getHeight() - hSize) * scroll);
		texHndl.drawTexture(bounds.getX(), bounds.getY() + sy, bounds.getWidth(), hSize, 0F);
		
		GlStateManager.popMatrix();
	}

	@Override
	public boolean onMouseClick(int mx, int my, int click)
	{
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
	public void onPanelEvent(IPanelEvent event)
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
