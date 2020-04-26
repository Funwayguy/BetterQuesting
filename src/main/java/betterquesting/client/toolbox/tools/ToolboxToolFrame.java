package betterquesting.client.toolbox.tools;

import betterquesting.api.client.toolbox.IToolboxTool;
import betterquesting.api.properties.NativeProps;
import betterquesting.api2.client.gui.controls.PanelButtonQuest;
import betterquesting.api2.client.gui.panels.lists.CanvasQuestLine;
import betterquesting.client.gui2.editors.designer.PanelToolController;
import betterquesting.network.handlers.NetQuestEdit;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.NonNullList;
import org.lwjgl.glfw.GLFW;

import java.util.Collections;
import java.util.List;

public class ToolboxToolFrame implements IToolboxTool
{
	private CanvasQuestLine gui;
	
	@Override
	public void initTool(CanvasQuestLine gui)
	{
		this.gui = gui;
	}
    
    @Override
    public void refresh(CanvasQuestLine gui)
    {
    
    }
    
    @Override
    public void disableTool()
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
    public void onSelection(NonNullList<PanelButtonQuest> buttons)
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
        changeFrame(btnList);
		return true;
    }
    
    private void changeFrame(List<PanelButtonQuest> btnList)
    {
        boolean state = !btnList.get(0).getStoredValue().getValue().getProperty(NativeProps.MAIN);
        
        ListNBT dataList = new ListNBT();
        for(PanelButtonQuest btn : btnList)
        {
            btn.getStoredValue().getValue().setProperty(NativeProps.MAIN, state);
            
            CompoundNBT entry = new CompoundNBT();
            entry.putInt("questID", btn.getStoredValue().getID());
            entry.put("config", btn.getStoredValue().getValue().writeToNBT(new CompoundNBT()));
            dataList.add(entry);
        }
        
        CompoundNBT payload = new CompoundNBT();
        payload.put("data", dataList);
        payload.putInt("action", 0);
        NetQuestEdit.sendEdit(payload);
    }
    
    @Override
    public boolean onKeyPressed(int key, int scancode, int modifiers)
    {
	    if(PanelToolController.selected.size() <= 0 || key != GLFW.GLFW_KEY_ENTER) return false;
	    
	    changeFrame(PanelToolController.selected);
        return true;
    }
    
    @Override
    public boolean useSelection()
    {
        return true;
    }
}
