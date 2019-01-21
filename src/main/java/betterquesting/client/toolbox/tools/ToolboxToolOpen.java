package betterquesting.client.toolbox.tools;

import betterquesting.api.client.toolbox.IToolboxTool;
import betterquesting.api2.client.gui.controls.PanelButtonQuest;
import betterquesting.client.gui2.CanvasQuestLine;
import betterquesting.client.gui2.GuiQuest;
import betterquesting.questing.QuestDatabase;
import net.minecraft.client.Minecraft;

import java.util.List;

public class ToolboxToolOpen implements IToolboxTool
{
	private CanvasQuestLine gui;
	
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
			int qID = QuestDatabase.INSTANCE.getID(btn.getStoredValue());
			
			Minecraft mc = Minecraft.getMinecraft();
			mc.displayGuiScreen(new GuiQuest(mc.currentScreen, qID));
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
	public boolean onMouseScroll(int mx, int my, int scroll)
	{
	    return false;
	}

	@Override
	public boolean onKeyPressed(char c, int key)
	{
	    return false;
	}
 
	@Override
	public boolean clampScrolling()
	{
		return true;
	}
}
