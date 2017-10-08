package adv_director.rw2.api.client.gui.panels.lists;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import adv_director.api.utils.RenderUtils;
import adv_director.rw2.api.client.gui.controls.IValueIO;
import adv_director.rw2.api.client.gui.events.PanelEvent;
import adv_director.rw2.api.client.gui.misc.ComparatorGuiDepth;
import adv_director.rw2.api.client.gui.misc.GuiRectangle;
import adv_director.rw2.api.client.gui.misc.GuiRectangleDynamic;
import adv_director.rw2.api.client.gui.misc.IGuiRect;
import adv_director.rw2.api.client.gui.panels.IGuiCanvas;
import adv_director.rw2.api.client.gui.panels.IGuiPanel;

public class CanvasScrolling implements IGuiCanvas
{
	private final List<IGuiPanel> guiPanels = new ArrayList<IGuiPanel>();
	private IGuiRect transform = GuiRectangle.ZERO;
	private final GuiRectangleDynamic innerTransform;
	
	private int maxScrollX = 0;
	private int maxScrollY = 0;
	private IValueIO<Float> scrollX;
	private IValueIO<Float> scrollY;
	private boolean isDragging = false;
	private float dragSX = 0;
	private float dragSY = 0;
	private int dragMX = 0;
	private int dragMY = 0;
	private int scrollSpeed = 12;
	
	public CanvasScrolling()
	{
		innerTransform = new GuiRectangleDynamic(0, 0, 0, 0, 0);
		innerTransform.setParent(transform);
		
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
				this.v = MathHelper.clamp_float(value, 0F, 1F);
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
				this.v = MathHelper.clamp_float(value, 0F, 1F);
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
	
	@Override
	public void setTransform(IGuiRect rect)
	{
		this.transform = rect != null? rect : GuiRectangle.ZERO;
	}
	
	private int lsx = 0;
	private int lsy = 0;
	
	@Override
	public void drawPanel(int mx, int my, float partialTick)
	{
		if(isDragging && (Mouse.isButtonDown(0) || Mouse.isButtonDown(2)))
		{
			int dx = dragMX - mx;
			int dy = dragMY - my;
			
			if(maxScrollX > 0)
			{
				float dsx = dx / (float)maxScrollX + dragSX;
				scrollX.writeValue(dsx);
			}
			
			if(maxScrollY > 0)
			{
				float dsy = dy / (float)maxScrollY + dragSY;
				scrollY.writeValue(dsy);
			}
		} else if(isDragging)
		{
			this.isDragging = false;
		}
		
		if(lsx != getScrollX() || lsy != getScrollY())
		{
			this.updatePanelScroll();
		}
		
		GlStateManager.pushMatrix();
		
		GL11.glEnable(GL11.GL_SCISSOR_TEST);
		RenderUtils.guiScissor(Minecraft.getMinecraft(), transform.getX(), transform.getY(), transform.getWidth(), transform.getHeight());
		
		List<IGuiPanel> tmp = new ArrayList<IGuiPanel>(guiPanels);
		
		for(IGuiPanel panel : tmp)
		{
			panel.drawPanel(mx, my, partialTick);
		}
		
		GL11.glDisable(GL11.GL_SCISSOR_TEST);
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
	public void onKeyTyped(char c, int keycode)
	{
		List<IGuiPanel> tmp = new ArrayList<IGuiPanel>(guiPanels);
		
		for(IGuiPanel panel : tmp)
		{
			panel.onKeyTyped(c, keycode);
		}
	}
	
	@Override
	public void onPanelEvent(PanelEvent event)
	{
		List<IGuiPanel> tmp = new ArrayList<IGuiPanel>(guiPanels);
		
		for(IGuiPanel panel : tmp)
		{
			panel.onPanelEvent(event);
		}
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
		
		if(panel.getTransform().getParent() == null)
		{
			panel.getTransform().setParent(innerTransform);
		}
		
		Collections.sort(guiPanels, ComparatorGuiDepth.INSTANCE);
		panel.initPanel();
		
		int px = getScrollX();
		int py = getScrollY();
		maxScrollX = Math.max(maxScrollX, panel.getTransform().getWidth() - transform.getWidth());
		maxScrollY = Math.max(maxScrollY, panel.getTransform().getHeight() - transform.getHeight());
		
		if(maxScrollX > 0)
		{
			this.scrollX.writeValue(px / (float)maxScrollX);
		}
		
		if(maxScrollY > 0)
		{
			this.scrollY.writeValue(py / (float)maxScrollY);
		}
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
			maxScrollX = Math.max(maxScrollX, panel.getTransform().getWidth() - transform.getWidth());
			maxScrollY = Math.max(maxScrollY, panel.getTransform().getHeight() - transform.getHeight());
		}
		
		updatePanelScroll();
	}
	
	private void updatePanelScroll()
	{
		innerTransform.offX = -getScrollX();
		innerTransform.offY = -getScrollY();
	}
	
	@Override
	public List<IGuiPanel> getAllPanels()
	{
		return guiPanels;
	}
}
