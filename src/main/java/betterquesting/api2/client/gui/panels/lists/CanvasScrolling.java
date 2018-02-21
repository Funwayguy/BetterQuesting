package betterquesting.api2.client.gui.panels.lists;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.BufferUtils;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Matrix4f;
import betterquesting.api.utils.RenderUtils;
import betterquesting.api2.client.gui.controls.IValueIO;
import betterquesting.api2.client.gui.misc.ComparatorGuiDepth;
import betterquesting.api2.client.gui.misc.GuiAlign;
import betterquesting.api2.client.gui.misc.GuiPadding;
import betterquesting.api2.client.gui.misc.GuiRectangle;
import betterquesting.api2.client.gui.misc.GuiTransform;
import betterquesting.api2.client.gui.misc.IGuiRect;
import betterquesting.api2.client.gui.panels.IGuiCanvas;
import betterquesting.api2.client.gui.panels.IGuiPanel;

public class CanvasScrolling implements IGuiCanvas
{
	private final List<IGuiPanel> guiPanels = new ArrayList<IGuiPanel>();
	private final IGuiRect transform;
	
	// Max scrolling bounds
	private int maxScrollX = 0;
	private int maxScrollY = 0;
	// Scroll and zoom drivers
	private IValueIO<Float> scrollX;
	private IValueIO<Float> scrollY;
	private IValueIO<Float> zoomScale;
	
	private boolean isDragging = false; // Mouse buttons held for dragging
	private boolean hasDragged = false; // Dragging used. Don't fire onMouseRelease
	private int scrollSpeed = 12;
	private boolean zoomMode = false;
	
	// Starting drag scroll values
	private float dragSX = 0;
	private float dragSY = 0;
	// Starting drag mouse positions
	private int dragMX = 0;
	private int dragMY = 0;
	// Last known scroll position (unscaled)
	private int lsx = 0;
	private int lsy = 0;
	// Scaled drawing offset
	private float drawDX = 0;
	private float drawDY = 0;
	
	public CanvasScrolling(IGuiRect rect)
	{
		this.transform = rect;
		
		// Dummy value drivers
		
		scrollX = new IValueIO<Float>()
		{
			private float v = 0F;
			
			@Override
			public Float readValue()
			{
				return v;
			}
			
			@Override
			public void writeValue(Float value)
			{
				this.v = MathHelper.clamp(value, 0F, 1F);
			}
		};
		
		scrollY = new IValueIO<Float>()
		{
			private float v = 0F;
			
			@Override
			public Float readValue()
			{
				return v;
			}
			
			@Override
			public void writeValue(Float value)
			{
				this.v = MathHelper.clamp(value, 0F, 1F);
			}
		};
		
		zoomScale = new IValueIO<Float>()
		{
			private float v = 1F;
			
			@Override
			public Float readValue()
			{
				return v;
			}
			
			@Override
			public void writeValue(Float value)
			{
				this.v = MathHelper.clamp(value, 0.25F, 2F);
			}
		};
	}
	
	public CanvasScrolling setScrollDriverX(IValueIO<Float> driver)
	{
		this.scrollX = driver;
		return this;
	}
	
	public CanvasScrolling setScrollDriverY(IValueIO<Float> driver)
	{
		this.scrollY = driver;
		return this;
	}
	
	public CanvasScrolling setZoomDriver(IValueIO<Float> driver)
	{
		this.zoomScale = driver;
		return this;
	}
	
	public CanvasScrolling setScrollSpeed(int dx)
	{
		this.scrollSpeed = dx;
		return this;
	}
	
	public CanvasScrolling enableZoomScroll(boolean enable)
	{
		this.zoomMode = enable;
		return this;
	}
	
	public int getScrollX()
	{
		return Math.round(maxScrollX * scrollX.readValue());
	}
	
	public int getScrollY()
	{
		return Math.round(maxScrollY * scrollY.readValue());
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
		float zs = zoomScale.readValue();
		
		if(isDragging)
		{
			int dx = (int)((dragMX - mx) / zs);
			int dy = (int)((dragMY - my) / zs);
			
			if(maxScrollX > 0)
			{
				float dsx = dx / (float)maxScrollX + dragSX;
				scrollX.writeValue(dsx);
				
				if(!hasDragged && Math.abs(dragSX - scrollX.readValue()) > 0.05F)
				{
					hasDragged = true;
				}
			}
			
			if(maxScrollY > 0)
			{
				float dsy = dy / (float)maxScrollY + dragSY;
				scrollY.writeValue(dsy);
				
				if(!hasDragged && Math.abs(dragSY - scrollY.readValue()) > 0.05F)
				{
					hasDragged = true;
				}
			}
		} else if(hasDragged)
		{
			hasDragged = false;
		}
		
		if(lsx != getScrollX() || lsy != getScrollY())
		{
			this.updatePanelScroll();
		}
		
		GlStateManager.pushMatrix();
		
		Minecraft mc = Minecraft.getMinecraft();
		RenderUtils.startScissor(mc, new GuiRectangle(transform));
		
		GlStateManager.translate(-lsx + drawDX, -lsy + drawDY, 0F);
		GlStateManager.scale(zs, zs, 1F);
		
		FloatBuffer fb = BufferUtils.createFloatBuffer(16);
		GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, fb);
		fb.rewind();
		Matrix4f fm = new Matrix4f();
		fm.load(fb);
		
		int smx = (int)((mx + lsx - drawDX) / zs);
		int smy = (int)((my + lsy - drawDY) / zs);
		List<IGuiPanel> tmp = new ArrayList<IGuiPanel>(guiPanels);
		
		for(IGuiPanel panel : tmp)
		{
			panel.drawPanel(smx, smy, partialTick);
		}
		
		RenderUtils.endScissor(mc);
		GlStateManager.popMatrix();
	}
	
	@Override
	public boolean onMouseClick(int mx, int my, int click)
	{
		if(!transform.contains(mx, my))
		{
			return false;
		}
		
		float zs = zoomScale.readValue();
		int smx = (int)((mx + lsx - drawDX) / zs);
		int smy = (int)((my + lsy - drawDY) / zs);
		
		List<IGuiPanel> tmp = new ArrayList<IGuiPanel>(guiPanels);
		Collections.reverse(tmp);
		boolean used = false;
		
		for(IGuiPanel panel : tmp)
		{
			used = panel.onMouseClick(smx, smy, click);
			
			if(used)
			{
				break;
			}
		}
		
		if(!used && (click == 0 || click == 2))
		{
			dragSX = scrollX.readValue();
			dragSY = scrollY.readValue();
			dragMX = mx;
			dragMY = my;
			isDragging = true;
		}
		
		return used;
	}
	
	@Override
	public boolean onMouseRelease(int mx, int my, int click)
	{
		boolean used = false;
		
		if(!hasDragged)
		{
			if(!transform.contains(mx, my))
			{
				return false;
			}
			
			float zs = zoomScale.readValue();
			int smx = (int)((mx + lsx - drawDX) / zs);
			int smy = (int)((my + lsy - drawDY) / zs);
			
			List<IGuiPanel> tmp = new ArrayList<IGuiPanel>(guiPanels);
			Collections.reverse(tmp);
			
			for(IGuiPanel panel : tmp)
			{
				used = panel.onMouseRelease(smx, smy, click);
				
				if(used)
				{
					break;
				}
			}
		}
		
		if(isDragging)
		{
			if(!Mouse.isButtonDown(0) && !Mouse.isButtonDown(2))
			{
				isDragging = false;
			}
			
			return true;
		}
		
		return used;
	}
	
	@Override
	public boolean onMouseScroll(int mx, int my, int scroll)
	{
		if(scroll == 0 || !transform.contains(mx, my))
		{
			return false;
		}
		
		float zs = zoomScale.readValue();
		int smx = (int)((mx + lsx - drawDX) / zs);
		int smy = (int)((my + lsy - drawDY) / zs);
		
		List<IGuiPanel> tmp = new ArrayList<IGuiPanel>(guiPanels);
		Collections.reverse(tmp);
		boolean used = false;
		
		for(IGuiPanel panel : tmp)
		{
			used = panel.onMouseScroll(smx, smy, scroll);
			
			if(used)
			{
				break;
			}
		}
		
		if(!used)
		{
			if(zoomMode)
			{
				float dy = -scroll * 0.05F;// * scrollSpeed;
				float cs = zoomScale.readValue();
				
				zoomScale.writeValue(cs + dy);
				
				this.refreshScrollBounds();
			} else if(maxScrollY > 0)
			{
				float dy = (scroll * scrollSpeed) / (float)maxScrollY;
				float cs = scrollY.readValue();
				
				if(!((dy < 0F && cs <= 0F) || (dy > 0F && cs >= 1F)))
				{
					scrollY.writeValue(cs + dy);
					this.updatePanelScroll();
				}
			}
		}
		
		return used;
	}
	
	@Override
	public boolean onKeyTyped(char c, int keycode)
	{
		List<IGuiPanel> tmp = new ArrayList<IGuiPanel>(guiPanels);
		boolean used = false;
		
		for(IGuiPanel entry : tmp)
		{
			if(entry.onKeyTyped(c, keycode))
			{
				used = true;
				break;
			}
		}
		
		return used;
	}
	
	@Override
	public List<String> getTooltip(int mx, int my)
	{
		float zs = zoomScale.readValue();
		int smx = (int)((mx + lsx - drawDX) / zs);
		int smy = (int)((my + lsy - drawDY) / zs);
		
		List<IGuiPanel> tmp = new ArrayList<IGuiPanel>(guiPanels);
		Collections.reverse(tmp);
		
		for(IGuiPanel entry : tmp)
		{
			List<String> tt = entry.getTooltip(smx, smy);
			
			if(tt != null && tt.size() > 0)
			{
				return tt;
			}
		}
		
		return new ArrayList<String>();
	}
	
	@Override
	public void addPanel(IGuiPanel panel)
	{
		if(panel == null || guiPanels.contains(panel))
		{
			return;
		}
		
		guiPanels.add(panel);
		panel.getTransform().setParent(transform);
		Collections.sort(guiPanels, ComparatorGuiDepth.INSTANCE);
		panel.initPanel();
		
		this.refreshScrollBounds();
	}
	
	@Override
	public boolean removePanel(IGuiPanel panel)
	{
		boolean b = guiPanels.remove(panel);
		
		if(b)
		{
			this.refreshScrollBounds();
		}
		
		return b;
	}
	
	private void refreshScrollBounds()
	{
		int px = getScrollX();
		int py = getScrollY();
		
		maxScrollX = 0;
		maxScrollY = 0;
		
		List<IGuiPanel> tmp = new ArrayList<IGuiPanel>(guiPanels);
		float zs = zoomScale.readValue();

		for(IGuiPanel panel : tmp)
		{
			maxScrollX = (int)Math.max(maxScrollX, panel.getTransform().getX() - transform.getX() + panel.getTransform().getWidth() - (transform.getWidth() / zs));
			maxScrollY = (int)Math.max(maxScrollY, panel.getTransform().getY() - transform.getY() + panel.getTransform().getHeight() - (transform.getHeight() / zs));
		}
		
		if(maxScrollX > 0)
		{
			this.scrollX.writeValue(px / (float)maxScrollX);
		}
		
		if(maxScrollY > 0)
		{
			this.scrollY.writeValue(py / (float)maxScrollY);
		}
		
		updatePanelScroll();
	}
	
	private void updatePanelScroll()
	{
		lsx = this.getScrollX();
		lsy = this.getScrollY();
		
		float zs = zoomScale.readValue();
		float sdx = transform.getX() + lsx;
		float sdy = transform.getY() + lsy;
		sdx = sdx - sdx * zs;
		sdy = sdy - sdy * zs;
		drawDX = sdx;
		drawDY = sdy;
	}
	
	@Override
	public List<IGuiPanel> getAllPanels()
	{
		return guiPanels;
	}
}
