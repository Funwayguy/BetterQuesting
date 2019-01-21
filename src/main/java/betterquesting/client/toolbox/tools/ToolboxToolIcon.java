package betterquesting.client.toolbox.tools;

import betterquesting.api.client.toolbox.IToolboxTool;
import betterquesting.api2.client.gui.controls.PanelButtonQuest;
import betterquesting.client.gui2.CanvasQuestLine;
import betterquesting.client.toolbox.GuiToolIconProxy;
import net.minecraft.client.Minecraft;

import java.util.List;

public class ToolboxToolIcon implements IToolboxTool
{
	private CanvasQuestLine gui;
	
	@Override
	public void initTool(CanvasQuestLine gui)
	{
		this.gui = gui;
	}
	
	@Override
	public void disableTool()
	{
	}
	
	@Override
	public boolean onMouseClick(int mx, int my, int click)
	{
		if(click != 0)
		{
			return false;
		}
		
		PanelButtonQuest btn = gui.getButtonAt(mx, my);
		
		if(btn != null)
		{
		    Minecraft mc = Minecraft.getMinecraft();
			mc.displayGuiScreen(new GuiToolIconProxy(mc.currentScreen, btn.getStoredValue()));
			return true;
		}
		
		return false;
	}
	
	@Override
    public boolean onMouseRelease(int mx, int my, int click)
    {
        return false;
    }
	
	@Override
	public boolean onMouseScroll(int mx, int my, int scroll)
	{
	    return false;
	}
	
	@Override
	public boolean onKeyPressed(char c, int keyCode)
	{
	    return false;
	}

	@Override
	public void drawCanvas(int mx, int my, float partialTick)
	{
	}
	
	@Override
    public void drawOverlay(int mx, int my, float partialTick)
    {
    }
    
    @Override
    public List<String> getTooltip(int mx, int my)
    {
        return null;
    }

	@Override
	public boolean clampScrolling()
	{
		return true;
	}
}
