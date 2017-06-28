package adv_director.rw2.api.client.gui.panels.lists;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.Rectangle;
import adv_director.api.utils.RenderUtils;
import adv_director.rw2.api.client.gui.controls.IValueIO;
import adv_director.rw2.api.client.gui.events.IPanelEvent;
import adv_director.rw2.api.client.gui.misc.GuiTransform;
import adv_director.rw2.api.client.gui.misc.PanelEntry;
import adv_director.rw2.api.client.gui.panels.IGuiCanvas;
import adv_director.rw2.api.client.gui.panels.IGuiPanel;

public class CanvasScrolling implements IGuiCanvas
{
	private final List<PanelEntry> guiPanels = new ArrayList<PanelEntry>();
	private final Rectangle bounds = new Rectangle(0, 0, 1, 1);
	private IGuiPanel parent;
	
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
	public void updateBounds(Rectangle bounds)
	{
		this.bounds.setBounds(bounds);
	}
	
	@Override
	public void initPanel()
	{
	}
	
	@Override
	public Rectangle getBounds()
	{
		return bounds;
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
		RenderUtils.guiScissor(Minecraft.getMinecraft(), bounds.getX(), bounds.getY(), bounds.getWidth(), bounds.getHeight());
		
		List<PanelEntry> tmp = new ArrayList<PanelEntry>(guiPanels);
		
		for(PanelEntry entry : tmp)
		{
			IGuiPanel panel = entry.getPanel();
			panel.drawPanel(mx, my, partialTick);
		}
		
		GL11.glDisable(GL11.GL_SCISSOR_TEST);
		GlStateManager.popMatrix();
	}
	
	@Override
	public boolean onMouseClick(int mx, int my, int click)
	{
		if(!bounds.contains(mx, my))
		{
			return false;
		}
		
		List<PanelEntry> tmp = new ArrayList<PanelEntry>(guiPanels);
		Collections.reverse(tmp);
		boolean used = false;
		
		for(PanelEntry entry : tmp)
		{
			IGuiPanel panel = entry.getPanel();
			
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
		if(scroll == 0 || !bounds.contains(mx, my))
		{
			return false;
		}
		
		List<PanelEntry> tmp = new ArrayList<PanelEntry>(guiPanels);
		Collections.reverse(tmp);
		boolean used = false;
		
		for(PanelEntry entry : tmp)
		{
			IGuiPanel panel = entry.getPanel();
			
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
		List<PanelEntry> tmp = new ArrayList<PanelEntry>(guiPanels);
		
		for(PanelEntry entry : tmp)
		{
			IGuiPanel panel = entry.getPanel();
			
			panel.onKeyTyped(c, keycode);
		}
	}
	
	@Override
	public void onPanelEvent(IPanelEvent event)
	{
		List<PanelEntry> tmp = new ArrayList<PanelEntry>(guiPanels);
		
		for(PanelEntry entry : tmp)
		{
			IGuiPanel panel = entry.getPanel();
			
			panel.onPanelEvent(event);
		}
	}
	
	@Override
	public List<String> getTooltip(int mx, int my)
	{
		List<PanelEntry> tmp = new ArrayList<PanelEntry>(guiPanels);
		Collections.reverse(tmp);
		
		int sx = Math.round(maxScrollX * scrollX.readValue());
		int sy = Math.round(maxScrollY * scrollY.readValue());
		
		for(PanelEntry entry : tmp)
		{
			List<String> tt = entry.getPanel().getTooltip(mx + sx, my + sy);
			
			if(tt != null && tt.size() > 0)
			{
				return tt;
			}
		}
		
		return new ArrayList<String>();
	}
	
	@Override
	public PanelEntry addPanel(GuiTransform transform, IGuiPanel panel)
	{
		if(transform == null || panel == null)
		{
			return null;
		}
		
		PanelEntry entry = new PanelEntry(transform, panel);
		guiPanels.add(entry);
		Collections.sort(guiPanels);
		
		panel.setParentPanel(this);
		panel.updateBounds(transform.applyTransform(bounds));
		panel.initPanel();
		
		int px = getScrollX();
		int py = getScrollY();
		maxScrollX = Math.max(maxScrollX, panel.getBounds().getWidth() - bounds.getWidth());
		maxScrollY = Math.max(maxScrollY, panel.getBounds().getHeight() - bounds.getHeight());
		
		if(maxScrollX > 0)
		{
			this.scrollX.writeValue(px / (float)maxScrollX);
		}
		
		if(maxScrollY > 0)
		{
			this.scrollY.writeValue(py / (float)maxScrollY);
		}
		
		return entry;
	}
	
	@Override
	public boolean removePanel(IGuiPanel panel)
	{
		Iterator<PanelEntry> iter = guiPanels.iterator();
		
		while(iter.hasNext())
		{
			PanelEntry entry = iter.next();
			
			if(entry.getPanel() == panel)
			{
				iter.remove();
				refreshScrollBounds();
				return true;
			}
		}
		
		return false;
	}
	
	private void refreshScrollBounds()
	{
		maxScrollX = 0;
		maxScrollY = 0;
		
		List<PanelEntry> tmp = new ArrayList<PanelEntry>(guiPanels);
		
		for(PanelEntry entry : tmp)
		{
			IGuiPanel panel = entry.getPanel();
			maxScrollX = Math.max(maxScrollX, panel.getBounds().getWidth() - bounds.getWidth());
			maxScrollY = Math.max(maxScrollY, panel.getBounds().getHeight() - bounds.getHeight());
		}
		
		updatePanelScroll();
	}
	
	private void updatePanelScroll()
	{
		List<PanelEntry> tmp = new ArrayList<PanelEntry>(guiPanels);
		
		for(PanelEntry entry : tmp)
		{
			IGuiPanel panel = entry.getPanel();
			Rectangle rec = entry.getTransform().applyTransform(bounds);
			rec.translate(-getScrollX(), -getScrollY());
			panel.updateBounds(rec);
		}
	}
	
	@Override
	public List<PanelEntry> getAllPanels()
	{
		return guiPanels;
	}
}
