package betterquesting.client.toolbox.tools;

import betterquesting.api.client.toolbox.IToolboxTool;
import betterquesting.api.questing.IQuestLine;
import betterquesting.api2.client.gui.controls.PanelButtonQuest;
import betterquesting.api2.client.gui.panels.lists.CanvasQuestLine;
import betterquesting.client.gui2.editors.designer.PanelToolController;
import betterquesting.network.handlers.NetChapterEdit;
import betterquesting.questing.QuestLineDatabase;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.NonNullList;
import org.lwjgl.glfw.GLFW;

public class ToolboxToolRemove implements IToolboxTool
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
	public boolean onMouseClick(int mx, int my, int click)
	{
		if(click != 0 || !gui.getTransform().contains(mx, my))
		{
			return false;
		}
		
		IQuestLine line = gui.getQuestLine();
		PanelButtonQuest btn = gui.getButtonAt(mx, my);
		
		if(line != null && btn != null)
		{
		    if(PanelToolController.selected.size() > 0)
            {
                if(!PanelToolController.selected.contains(btn)) return false;
                for(PanelButtonQuest b : PanelToolController.selected) line.removeID(b.getStoredValue().getID());
            } else
            {
                int qID = btn.getStoredValue().getID();
                line.removeID(qID);
            }
		    
            // Sync Line
            CompoundNBT chPayload = new CompoundNBT();
            ListNBT cdList = new ListNBT();
            CompoundNBT cTag = new CompoundNBT();
            cTag.putInt("chapterID", QuestLineDatabase.INSTANCE.getID(line));
            cTag.put("config", line.writeToNBT(new CompoundNBT(), null));
            cdList.add(cTag);
            chPayload.put("data", cdList);
            chPayload.putInt("action", 0);
            NetChapterEdit.sendEdit(chPayload);
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
	public boolean onKeyPressed(int key, int scancode, int modifiers)
	{
	    if(PanelToolController.selected.size() > 0 && key == GLFW.GLFW_KEY_ENTER)
        {
            IQuestLine line = gui.getQuestLine();
            for(PanelButtonQuest b : PanelToolController.selected) line.removeID(b.getStoredValue().getID());
		    
            // Sync Line
            CompoundNBT chPayload = new CompoundNBT();
            ListNBT cdList = new ListNBT();
            CompoundNBT cTag = new CompoundNBT();
            cTag.putInt("chapterID", QuestLineDatabase.INSTANCE.getID(line));
            cTag.put("config", line.writeToNBT(new CompoundNBT(), null));
            cdList.add(cTag);
            chPayload.put("data", cdList);
            chPayload.putInt("action", 0);
            NetChapterEdit.sendEdit(chPayload);
			return true;
        }
        
	    return false;
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
