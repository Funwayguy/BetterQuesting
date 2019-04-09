package betterquesting.client.toolbox.tools;

import betterquesting.api.client.toolbox.IToolboxTool;
import betterquesting.api.enums.EnumPacketAction;
import betterquesting.api.network.QuestingPacket;
import betterquesting.api2.client.gui.controls.PanelButtonQuest;
import betterquesting.api2.client.gui.panels.lists.CanvasQuestLine;
import betterquesting.client.gui2.editors.designer.PanelToolController;
import betterquesting.network.PacketSender;
import betterquesting.network.PacketTypeNative;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import org.lwjgl.input.Keyboard;

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
    public List<String> getTooltip(int mx, int my)
    {
        return null;
    }
	
	@Override
	public boolean onMouseClick(int mx, int my, int click)
	{
		if(click != 0 || !gui.getTransform().contains(mx, my)) return false;
		
		PanelButtonQuest btn = gui.getButtonAt(mx, my);
		
		if(btn != null)
		{
		    if(PanelToolController.selected.size() > 0)
            {
                if(!PanelToolController.selected.contains(btn)) return false;
                
                for(PanelButtonQuest b : PanelToolController.selected)
                {
                    NBTTagCompound tags = new NBTTagCompound();
                    tags.setInteger("action", EnumPacketAction.REMOVE.ordinal()); // Remove quest
                    tags.setInteger("questID", b.getStoredValue().getID());
			        PacketSender.INSTANCE.sendToServer(new QuestingPacket(PacketTypeNative.QUEST_EDIT.GetLocation(), tags));
                }
            } else
            {
                NBTTagCompound tags = new NBTTagCompound();
                tags.setInteger("action", EnumPacketAction.REMOVE.ordinal()); // Remove quest
                tags.setInteger("questID", btn.getStoredValue().getID());
                PacketSender.INSTANCE.sendToServer(new QuestingPacket(PacketTypeNative.QUEST_EDIT.GetLocation(), tags));
            }
            
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
	public boolean onKeyPressed(char c, int key)
	{
	    if(PanelToolController.selected.size() > 0 && key == Keyboard.KEY_RETURN)
        {
            int[] bulkIDs = new int[PanelToolController.selected.size()];
            for(int i = 0; i < bulkIDs.length; i++)
            {
                bulkIDs[i] = PanelToolController.selected.get(i).getStoredValue().getID();
            }
            
            NBTTagCompound tags = new NBTTagCompound();
            tags.setInteger("action", EnumPacketAction.REMOVE.ordinal()); // Complete quest
            tags.setInteger("questID", -1);
            tags.setIntArray("bulkIDs", bulkIDs);
            PacketSender.INSTANCE.sendToServer(new QuestingPacket(PacketTypeNative.QUEST_EDIT.GetLocation(), tags));
            
            return true;
        }
        
        return false;
	}

	@Override
	public boolean clampScrolling()
	{
		return true;
	}
	
	@Override
    public void onSelection(NonNullList<PanelButtonQuest> buttons)
    {
    }
	
	@Override
    public boolean useSelection()
    {
        return true; // TODO: Fix the database before re-enabling this
    }
}
