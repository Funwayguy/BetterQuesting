package betterquesting.api2.client.gui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.input.Mouse;
import betterquesting.api2.client.gui.misc.ComparatorGuiDepth;
import betterquesting.api2.client.gui.misc.GuiRectangle;
import betterquesting.api2.client.gui.misc.IGuiRect;
import betterquesting.api2.client.gui.panels.IGuiCanvas;
import betterquesting.api2.client.gui.panels.IGuiPanel;

public class GuiScreenCanvas extends GuiScreen implements IGuiCanvas
{
	private final List<IGuiPanel> guiPanels = new ArrayList<IGuiPanel>();
	private final GuiRectangle transform = new GuiRectangle(0, 0, 0, 0, 0);
	
	public final GuiScreen parent;
	
	public GuiScreenCanvas(GuiScreen parent)
	{
		this.parent = parent;
	}
	
	@Override
	public IGuiRect getTransform()
	{
		return transform;
	}
	
	/**
	 * Use initPanel() for embed support
	 */
	@Override
	@Deprecated
	public void initGui()
	{
		super.initGui();
		
		transform.w = this.width;
		transform.h = this.height;
		
		this.getAllPanels().clear();
		
		initPanel();
	}
	
	@Override
	public void initPanel()
	{
		List<IGuiPanel> tmp = new ArrayList<IGuiPanel>(guiPanels);
		
		for(IGuiPanel entry : tmp)
		{
			entry.initPanel();
		}
	}
	
	/**
	 * Use drawPanel() for embed support
	 */
	@Override
	@Deprecated
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
	
	/**
	 * Use panel buttons and the event broadcaster
	 */
	@Override
	@Deprecated
	public void actionPerformed(GuiButton button)
	{
	}
	
	private boolean[] mBtnState = new boolean[3];
	
	@Override
	public void handleMouseInput() throws IOException
	{
		super.handleMouseInput();
		
        int i = Mouse.getEventX() * width / mc.displayWidth;
        int j = height - Mouse.getEventY() * height / mc.displayHeight - 1;
        int k = Mouse.getEventButton();
        int SDX = (int)-Math.signum(Mouse.getEventDWheel());
        boolean flag = Mouse.getEventButtonState();
        
        if(k >= 0 && k < 3 && mBtnState[k] != flag)
        {
        	if(flag)
        	{
        		this.onMouseClick(i, j, k);
        	} else
        	{
        		this.onMouseRelease(i, j, k);
        	}
        	mBtnState[k] = flag;
        }
        
        if(SDX != 0)
        {
        	this.onMouseScroll(i, j, SDX);
        }
	}
	
	//@Override
	public void drawPanel(int mx, int my, float partialTick)
	{
		List<IGuiPanel> tmp = new ArrayList<IGuiPanel>(guiPanels);
		
		for(IGuiPanel entry : tmp)
		{
			entry.drawPanel(mx, my, partialTick);
		}
	}
	
	@Override
	public boolean onMouseClick(int mx, int my, int click)
	{
		List<IGuiPanel> tmp = new ArrayList<IGuiPanel>(guiPanels);
		Collections.reverse(tmp);
		boolean used = false;
		
		for(IGuiPanel entry : tmp)
		{
			used = entry.onMouseClick(mx, my, click);
			
			if(used)
			{
				break;
			}
		}
		
		return used;
	}
	
	@Override
	public boolean onMouseRelease(int mx, int my, int click)
	{
		List<IGuiPanel> tmp = new ArrayList<IGuiPanel>(guiPanels);
		Collections.reverse(tmp);
		boolean used = false;
		
		for(IGuiPanel entry : tmp)
		{
			used = entry.onMouseRelease(mx, my, click);
			
			if(used)
			{
				break;
			}
		}
		
		return used;
	}
	
	//@Override
	public boolean onMouseScroll(int mx, int my, int scroll)
	{
		List<IGuiPanel> tmp = new ArrayList<IGuiPanel>(guiPanels);
		Collections.reverse(tmp);
		boolean used = false;
		
		for(IGuiPanel entry : tmp)
		{
			used = entry.onMouseScroll(mx, my, scroll);
			
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
		List<IGuiPanel> tmp = new ArrayList<IGuiPanel>(guiPanels);
		
		for(IGuiPanel entry : tmp)
		{
			entry.onKeyTyped(c, keycode);
		}
	}
	
	@Override
	public List<String> getTooltip(int mx, int my)
	{
		List<IGuiPanel> tmp = new ArrayList<IGuiPanel>(guiPanels);
		Collections.reverse(tmp);
		
		for(IGuiPanel entry : tmp)
		{
			List<String> tt = entry.getTooltip(mx, my);
			
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
		Collections.sort(guiPanels, ComparatorGuiDepth.INSTANCE);
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
	
	@Override
    public boolean doesGuiPauseGame()
    {
        return false; // Halts packet handling if paused
    }
	
	/**
	 * Should be using PanelButton instead when using a Canvas
	 */
	@Override
	@Deprecated
	public <T extends GuiButton> T addButton(T button)
	{
		return super.addButton(button);
	}
}
