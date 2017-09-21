package adv_director.rw2.api.client.gui.panels.bars;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.input.Mouse;
import org.lwjgl.util.Rectangle;
import adv_director.core.AdvDirector;
import adv_director.rw2.api.client.gui.events.PanelEvent;
import adv_director.rw2.api.client.gui.misc.GuiPadding;
import adv_director.rw2.api.client.gui.misc.GuiRectangle;
import adv_director.rw2.api.client.gui.misc.IGuiRect;
import adv_director.rw2.api.client.gui.resources.IGuiTexture;
import adv_director.rw2.api.client.gui.resources.SlicedTexture;

public class PanelHScrollBar implements IScrollBar
{
	private static final IGuiTexture DEF_BACK = new SlicedTexture(new ResourceLocation(AdvDirector.MODID, "textures/gui/editor_gui_alt.png"), new Rectangle(64, 0, 16, 8), new GuiPadding(1, 1, 1, 1));
	private static final IGuiTexture DEF_HNDL = new SlicedTexture(new ResourceLocation(AdvDirector.MODID, "textures/gui/editor_gui_alt.png"), new Rectangle(64, 8, 16, 8), new GuiPadding(4, 3, 4, 3));
	
	private IGuiRect transform = GuiRectangle.ZERO;
	
	private IGuiTexture texBack = DEF_BACK;
	private IGuiTexture texHndl = DEF_HNDL;
	
	private float scroll = 0F;
	private float speed = 0.1F;
	private int hSize = 16;
	private boolean isDragging = false;
	
	@Override
	public PanelHScrollBar setHandleSize(int size)
	{
		this.hSize = size;
		return this;
	}
	
	@Override
	public PanelHScrollBar setBarTexture(IGuiTexture back, IGuiTexture handle)
	{
		this.texBack = back;
		this.texHndl = handle;
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
	public void setTransform(IGuiRect rect)
	{
		this.transform = rect != null? rect : GuiRectangle.ZERO;
	}
	
	@Override
	public void drawPanel(int mx, int my, float partialTick)
	{
		IGuiRect bounds = new GuiRectangle(this.getTransform());
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
		
		texBack.drawTexture(bounds.getX(), bounds.getY(), bounds.getWidth(), bounds.getHeight(), 0F);
		int sx = MathHelper.floor_float((bounds.getWidth() - hSize) * scroll);
		texHndl.drawTexture(bounds.getX() + sx, bounds.getY(), hSize, bounds.getHeight(), 0F);
		
		GlStateManager.popMatrix();
	}

	@Override
	public boolean onMouseClick(int mx, int my, int click)
	{
		IGuiRect bounds = new GuiRectangle(this.getTransform());
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
		IGuiRect bounds = new GuiRectangle(this.getTransform());
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
