package betterquesting.api2.client.gui.panels;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.CopyOnWriteArrayList;

import betterquesting.api2.client.gui.misc.ComparatorGuiDepth;
import betterquesting.api2.client.gui.misc.IGuiRect;

public class CanvasEmpty implements IGuiCanvas
{
	private final List<IGuiPanel> guiPanels = new CopyOnWriteArrayList<>();
	private final IGuiRect transform;
	private boolean enabled = true;
	
	public CanvasEmpty(IGuiRect rect)
	{
		this.transform = rect;
	}
	
	@Override
	public IGuiRect getTransform()
	{
		return transform;
	}
	
	@Override
	public void initPanel()
	{
		this.guiPanels.clear();
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
	public void drawPanel(int mx, int my, float partialTick)
	{
		for(IGuiPanel entry : guiPanels)
		{
			if(entry.isEnabled())
			{
				entry.drawPanel(mx, my, partialTick);
			}
		}
	}
	
	@Override
	public boolean onMouseClick(int mx, int my, int click)
	{
		boolean used = false;
		
		ListIterator<IGuiPanel> pnIter = guiPanels.listIterator(guiPanels.size());
		
		while(pnIter.hasPrevious())
		{
			IGuiPanel entry = pnIter.previous();
			
			if(entry.isEnabled() && entry.onMouseClick(mx, my, click))
			{
				used = true;
				break;
			}
		}
		
		return used;
	}
	
	@Override
	public boolean onMouseRelease(int mx, int my, int click)
	{
		boolean used = false;
		
		ListIterator<IGuiPanel> pnIter = guiPanels.listIterator(guiPanels.size());
		
		while(pnIter.hasPrevious())
		{
			IGuiPanel entry = pnIter.previous();
			
			if(entry.isEnabled() && entry.onMouseRelease(mx, my, click))
			{
				used = true;
				break;
			}
		}
		
		return used;
	}
	
	@Override
	public boolean onMouseScroll(int mx, int my, int scroll)
	{
		boolean used = false;
		
		ListIterator<IGuiPanel> pnIter = guiPanels.listIterator(guiPanels.size());
		
		while(pnIter.hasPrevious())
		{
			IGuiPanel entry = pnIter.previous();
			
			if(entry.isEnabled() && entry.onMouseScroll(mx, my, scroll))
			{
				used = true;
				break;
			}
		}
		
		return used;
	}
	
	@Override
	public boolean onKeyTyped(char c, int keycode)
	{
		boolean used = false;
		
		ListIterator<IGuiPanel> pnIter = guiPanels.listIterator(guiPanels.size());
		
		while(pnIter.hasPrevious())
		{
			IGuiPanel entry = pnIter.previous();
			
			if(entry.isEnabled() && entry.onKeyTyped(c, keycode))
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
		ListIterator<IGuiPanel> pnIter = guiPanels.listIterator(guiPanels.size());
		List<String> tt;
		
		while(pnIter.hasPrevious())
		{
			IGuiPanel entry = pnIter.previous();
			
			if(!entry.isEnabled())
			{
				continue;
			}
			
			tt = entry.getTooltip(mx, my);
			
			if(tt != null && tt.size() > 0)
			{
				return tt;
			}
		}
		
		return null;
	}
	
	@Override
	public void addPanel(IGuiPanel panel)
	{
		if(panel == null || guiPanels.contains(panel))
		{
			return;
		}
		
		guiPanels.add(panel);
		guiPanels.sort(ComparatorGuiDepth.INSTANCE);
		panel.getTransform().setParent(getTransform());
		panel.initPanel();
	}
	
	@Override
	public boolean removePanel(IGuiPanel panel)
	{
		return guiPanels.remove(panel);
	}
	
	@Override
	public List<IGuiPanel> getAllPanels()
	{
		return guiPanels;
	}
}
