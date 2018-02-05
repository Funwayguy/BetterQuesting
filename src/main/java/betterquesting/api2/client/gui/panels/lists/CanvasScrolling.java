package betterquesting.api2.client.gui.panels.lists;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.input.Mouse;
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
	
	// Represents the inner scrolling region and viewing window size
	private final GuiTransform innerTransform;
	
	private int maxScrollX = 0;
	private int maxScrollY = 0;
	private IValueIO<Float> scrollX;
	private IValueIO<Float> scrollY;
	private boolean isDragging = false; // Mouse buttons held for dragging
	private boolean hasDragged = false; // Dragging used. Don't fire onMouseRelease
	private float dragSX = 0;
	private float dragSY = 0;
	private int dragMX = 0;
	private int dragMY = 0;
	private int scrollSpeed = 12;
	
	public CanvasScrolling(IGuiRect rect)
	{
		this.transform = rect;
		this.innerTransform = new GuiTransform(GuiAlign.FULL_BOX, new GuiPadding(0, 0, 0, 0), 0);
		this.innerTransform.setParent(transform);
		
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
	
	public CanvasScrolling setScrollSpeed(int dx)
	{
		this.scrollSpeed = dx;
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
	
	public IGuiRect getInnerTransform()
	{
		return innerTransform;
	}
	
	private int lsx = 0;
	private int lsy = 0;
	
	@Override
	public void drawPanel(int mx, int my, float partialTick)
	{
		if(isDragging)
		{
			int dx = dragMX - mx;
			int dy = dragMY - my;
			
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
		
		List<IGuiPanel> tmp = new ArrayList<IGuiPanel>(guiPanels);
		
		for(IGuiPanel panel : tmp)
		{
			panel.drawPanel(mx, my, partialTick);
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
		
		List<IGuiPanel> tmp = new ArrayList<IGuiPanel>(guiPanels);
		Collections.reverse(tmp);
		boolean used = false;
		
		for(IGuiPanel panel : tmp)
		{
			used = panel.onMouseClick(mx, my, click);
			
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
			
			List<IGuiPanel> tmp = new ArrayList<IGuiPanel>(guiPanels);
			Collections.reverse(tmp);
			
			for(IGuiPanel panel : tmp)
			{
				used = panel.onMouseRelease(mx, my, click);
				
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
		
		List<IGuiPanel> tmp = new ArrayList<IGuiPanel>(guiPanels);
		Collections.reverse(tmp);
		boolean used = false;
		
		for(IGuiPanel panel : tmp)
		{
			used = panel.onMouseScroll(mx, my, scroll);
			
			if(used)
			{
				break;
			}
		}
		
		if(maxScrollY > 0)
		{
			float dy = (scroll * scrollSpeed) / (float)maxScrollY;
			float cs = scrollY.readValue();
			
			if(!used && !((dy < 0F && cs <= 0F) || (dy > 0F && cs >= 1F)))
			{
				scrollY.writeValue(cs + dy);
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
		List<IGuiPanel> tmp = new ArrayList<IGuiPanel>(guiPanels);
		Collections.reverse(tmp);
		
		int sx = Math.round(maxScrollX * scrollX.readValue());
		int sy = Math.round(maxScrollY * scrollY.readValue());
		
		for(IGuiPanel entry : tmp)
		{
			List<String> tt = entry.getTooltip(mx + sx, my + sy);
			
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
		panel.getTransform().setParent(innerTransform);
		Collections.sort(guiPanels, ComparatorGuiDepth.INSTANCE);
		panel.initPanel();
		
		int px = getScrollX();
		int py = getScrollY();
		maxScrollX = Math.max(maxScrollX, panel.getTransform().getX() - innerTransform.getX() + panel.getTransform().getWidth() - transform.getWidth());
		maxScrollY = Math.max(maxScrollY, panel.getTransform().getY() - innerTransform.getY() + panel.getTransform().getHeight() - transform.getHeight());
		
		if(maxScrollX > 0)
		{
			this.scrollX.writeValue(px / (float)maxScrollX);
		}
		
		if(maxScrollY > 0)
		{
			this.scrollY.writeValue(py / (float)maxScrollY);
		}
		
		this.updatePanelScroll();
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
		maxScrollX = 0;
		maxScrollY = 0;
		
		List<IGuiPanel> tmp = new ArrayList<IGuiPanel>(guiPanels);
		
		for(IGuiPanel panel : tmp)
		{
			maxScrollX = Math.max(maxScrollX, panel.getTransform().getX() - innerTransform.getX() + panel.getTransform().getWidth() - transform.getWidth());
			maxScrollY = Math.max(maxScrollY, panel.getTransform().getY() - innerTransform.getY() + panel.getTransform().getHeight() - transform.getHeight());
		}
		
		updatePanelScroll();
	}
	
	private void updatePanelScroll()
	{
		// Probably not the most ideal way of making this work
		innerTransform.getPadding().l = -getScrollX();
		innerTransform.getPadding().r = getScrollX();
		innerTransform.getPadding().t = -getScrollY();
		innerTransform.getPadding().b = getScrollY();
		lsx = this.getScrollX();
		lsy = this.getScrollY();
	}
	
	@Override
	public List<IGuiPanel> getAllPanels()
	{
		return guiPanels;
	}
}
