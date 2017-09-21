package adv_director.rw2.api.client.gui.panels.lists;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.lwjgl.util.Rectangle;
import adv_director.rw2.api.client.gui.events.PanelEvent;
import adv_director.rw2.api.client.gui.misc.GuiTransform;
import adv_director.rw2.api.client.gui.misc.PanelEntry;
import adv_director.rw2.api.client.gui.panels.IGuiCanvas;
import adv_director.rw2.api.client.gui.panels.IGuiPanel;

public class CanvasVList implements IGuiCanvas
{
	private final List<PanelEntry> guiPanels = new ArrayList<PanelEntry>();
	private final Rectangle bounds = new Rectangle(0, 0, 1, 1);
	private IGuiPanel parent;
	
	@Override
	public IGuiPanel getParentPanel()
	{
		return this.parent;
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
		List<PanelEntry> tmp = new ArrayList<PanelEntry>(guiPanels);
		
		for(PanelEntry entry : tmp)
		{
			entry.getPanel().setParentPanel(this);
			entry.getPanel().updateBounds(entry.getTransform().applyTransform(bounds));
			entry.getPanel().initPanel();
		}
	}
	
	@Override
	public Rectangle getBounds()
	{
		return bounds;
	}
	
	@Override
	public void drawPanel(int mx, int my, float partialTick)
	{
		List<PanelEntry> tmp = new ArrayList<PanelEntry>(guiPanels);
		
		for(PanelEntry entry : tmp)
		{
			IGuiPanel panel = entry.getPanel();
			panel.drawPanel(mx, my, partialTick);
		}
	}
	
	@Override
	public boolean onMouseClick(int mx, int my, int click)
	{
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
		
		return used;
	}
	
	@Override
	public boolean onMouseScroll(int mx, int my, int scroll)
	{
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
	public void onPanelEvent(PanelEvent event)
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
		
		for(PanelEntry entry : tmp)
		{
			List<String> tt = entry.getPanel().getTooltip(mx, my);
			
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
		
		int vHeight = 0;
		
		for(PanelEntry pe : this.getAllPanels())
		{
			vHeight = Math.max(vHeight, (pe.getPanel().getBounds().getY() - bounds.getY()) + pe.getPanel().getBounds().getHeight());
		}
		
		PanelEntry entry = new PanelEntry(transform, panel);
		guiPanels.add(entry);
		Collections.sort(guiPanels);
		
		panel.setParentPanel(this);
		Rectangle pb = transform.applyTransform(bounds);
		pb.translate(0, vHeight);
		panel.updateBounds(pb);
		panel.initPanel();
		
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
				return true;
			}
		}
		
		return false;
	}
	
	@Override
	public List<PanelEntry> getAllPanels()
	{
		return guiPanels;
	}
	
}
