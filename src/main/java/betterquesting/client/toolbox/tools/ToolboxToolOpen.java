package betterquesting.client.toolbox.tools;

import betterquesting.api.client.toolbox.IToolboxTool;
import betterquesting.api2.client.gui.controls.PanelButtonQuest;
import betterquesting.api2.client.gui.panels.lists.CanvasQuestLine;
import betterquesting.client.gui2.GuiQuest;
import net.minecraft.client.Minecraft;
import net.minecraft.util.NonNullList;

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
    public void refresh(CanvasQuestLine gui)
    {
    }
	
	@Override
	public boolean onMouseClick(int mx, int my, int click)
	{
		if(click != 0 || !gui.getTransform().contains(mx, my))
		{
			return false;
		}
		
		PanelButtonQuest btn = gui.getButtonAt(mx, my);
		
		if(btn != null)
		{
			int qID = btn.getStoredValue().getID();
			
			Minecraft mc = Minecraft.getInstance();
			mc.displayGuiScreen(new GuiQuest(mc.currentScreen, qID));
			return true;
		}
		
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
    public void onSelection(NonNullList<PanelButtonQuest> buttons)
    {
    }
	
	@Override
    public boolean useSelection()
    {
        return false;
    }
}
