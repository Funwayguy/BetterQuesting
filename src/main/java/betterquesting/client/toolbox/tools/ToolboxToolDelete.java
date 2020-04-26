package betterquesting.client.toolbox.tools;

import betterquesting.api.client.toolbox.IToolboxTool;
import betterquesting.api2.client.gui.controls.PanelButtonQuest;
import betterquesting.api2.client.gui.panels.lists.CanvasQuestLine;
import betterquesting.client.gui2.editors.designer.PanelToolController;
import betterquesting.network.handlers.NetQuestEdit;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.NonNullList;
import org.lwjgl.glfw.GLFW;

import java.util.Collections;
import java.util.List;

public class ToolboxToolDelete implements IToolboxTool
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
    public void refresh(CanvasQuestLine gui)
    {
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
	public boolean onMouseClick(int mx, int my, int click)
	{
		if(click != 0 || !gui.getTransform().contains(mx, my)) return false;
		
		PanelButtonQuest btn = gui.getButtonAt(mx, my);
		
		if(btn == null) return false;
		if(PanelToolController.selected.size() > 0 && !PanelToolController.selected.contains(btn)) return false;
		
        List<PanelButtonQuest> btnList = PanelToolController.selected.size() > 0 ? PanelToolController.selected : Collections.singletonList(btn);
        int[] questIDs = new int[btnList.size()];
        
        for(int i = 0; i < btnList.size(); i++)
        {
            questIDs[i] = btnList.get(i).getStoredValue().getID();
        }
        
        CompoundNBT payload = new CompoundNBT();
        payload.putIntArray("questIDs", questIDs);
        payload.putInt("action", 1);
        NetQuestEdit.sendEdit(payload);
        
        return true;
	}

	@Override
	public boolean onKeyPressed(int key, int scancode, int modifiers)
	{
	    if(PanelToolController.selected.size() <= 0 || key != GLFW.GLFW_KEY_ENTER) return false;
	    
        List<PanelButtonQuest> btnList = PanelToolController.selected;
        int[] questIDs = new int[btnList.size()];
        
        for(int i = 0; i < btnList.size(); i++)
        {
            questIDs[i] = btnList.get(i).getStoredValue().getID();
        }
        
        CompoundNBT payload = new CompoundNBT();
        payload.putIntArray("questIDs", questIDs);
        payload.putInt("action", 1);
        NetQuestEdit.sendEdit(payload);
        
        return true;
	}
	
	@Override
    public void onSelection(NonNullList<PanelButtonQuest> buttons)
    {
    }
	
	@Override
    public boolean useSelection()
    {
        return true;
    }
}
