package adv_director.rw2.api.client.gui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.input.Mouse;
import org.lwjgl.util.Rectangle;
import adv_director.rw2.api.client.gui.events.IPanelEvent;
import adv_director.rw2.api.client.gui.misc.GuiTransform;
import adv_director.rw2.api.client.gui.misc.PanelEntry;
import adv_director.rw2.api.client.gui.panels.IGuiCanvas;
import adv_director.rw2.api.client.gui.panels.IGuiPanel;

public class GuiScreenCanvas extends GuiScreen implements IGuiCanvas
{
	private final List<PanelEntry> guiPanels = new ArrayList<PanelEntry>();
	private final Rectangle bounds = new Rectangle(0, 0, 1, 1);
	private final GuiTransform transform;
	private final GuiScreen parent;
	
	public GuiScreenCanvas(GuiScreen parent, GuiTransform transform)
	{
		this.transform = transform;
		this.parent = parent;
	}
	
	@Override
	public void initGui()
	{
		super.initGui();
		
		this.getAllPanels().clear();
		
		Rectangle frame = transform.applyTransform(new Rectangle(0, 0, this.width, this.height));
		updateBounds(frame);
		initPanel();
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
	public void drawScreen(int mx, int my, float partialTick)
	{
		super.drawScreen(mx, my, partialTick);
		
		this.drawPanel(mx, my, partialTick);
		
		List<String> tt = this.getTooltip(mx, my);
		
		if(tt != null && tt.size() > 0)
		{
			this.drawHoveringText(tt, mx, my);
		}
	}
	
	@Override
	public void actionPerformed(GuiButton button)
	{
		if(button.id == 0)
		{
			mc.displayGuiScreen(parent);
		}
	}
	
	@Override
	public void handleMouseInput() throws IOException
	{
		super.handleMouseInput();
		
        int i = Mouse.getEventX() * width / mc.displayWidth;
        int j = height - Mouse.getEventY() * height / mc.displayHeight - 1;
        int k = Mouse.getEventButton();
        int SDX = (int)-Math.signum(Mouse.getEventDWheel());
        boolean flag = Mouse.getEventButtonState();
        
        if(flag)
        {
        	this.onMouseClick(i, j, k);
        }
        
        if(SDX != 0)
        {
        	this.onMouseScroll(i, j, SDX);
        }
	}
	
	@Override
	public IGuiPanel getParentPanel()
	{
		return null;
	}
	
	@Override
	public void setParentPanel(IGuiPanel panel)
	{
		// BASE SCREEN CANNOT HAVE PARENT
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
		
		PanelEntry entry = new PanelEntry(transform, panel);
		guiPanels.add(entry);
		Collections.sort(guiPanels);
		
		panel.setParentPanel(this);
		panel.updateBounds(transform.applyTransform(bounds));
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
	
	@Override
    public boolean doesGuiPauseGame()
    {
        return false; // Halts packet handling if paused
    }
}
