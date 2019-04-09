package betterquesting.client.toolbox.tools;

import betterquesting.api.client.toolbox.IToolboxTool;
import betterquesting.api.enums.EnumPacketAction;
import betterquesting.api.network.QuestingPacket;
import betterquesting.api.properties.NativeProps;
import betterquesting.api2.client.gui.controls.PanelButtonQuest;
import betterquesting.api2.client.gui.panels.lists.CanvasQuestLine;
import betterquesting.client.gui2.editors.designer.PanelToolController;
import betterquesting.network.PacketSender;
import betterquesting.network.PacketTypeNative;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import org.lwjgl.input.Keyboard;

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
		
		if(PanelToolController.selected.size() > 0)
        {
            if(!PanelToolController.selected.contains(btn)) return false;
            
            boolean state = !btn.getStoredValue().getValue().getProperty(NativeProps.MAIN);
            
            for(PanelButtonQuest b : PanelToolController.selected)
            {
                b.getStoredValue().getValue().setProperty(NativeProps.MAIN, state);
                
                NBTTagCompound base = new NBTTagCompound();
                base.setTag("config", b.getStoredValue().getValue().writeToNBT(new NBTTagCompound()));
                base.setTag("progress", b.getStoredValue().getValue().writeProgressToNBT(new NBTTagCompound(), null));
                
                NBTTagCompound tags = new NBTTagCompound();
                tags.setInteger("action", EnumPacketAction.EDIT.ordinal()); // Action: Update data
                tags.setInteger("questID", b.getStoredValue().getID());
                tags.setTag("data", base);
                PacketSender.INSTANCE.sendToServer(new QuestingPacket(PacketTypeNative.QUEST_EDIT.GetLocation(), tags));
            }
        } else
        {
            boolean state = !btn.getStoredValue().getValue().getProperty(NativeProps.MAIN);
            btn.getStoredValue().getValue().setProperty(NativeProps.MAIN, state);
            
            NBTTagCompound base = new NBTTagCompound();
            base.setTag("config", btn.getStoredValue().getValue().writeToNBT(new NBTTagCompound()));
            base.setTag("progress", btn.getStoredValue().getValue().writeProgressToNBT(new NBTTagCompound(), null));
            
            NBTTagCompound tags = new NBTTagCompound();
            tags.setInteger("action", EnumPacketAction.EDIT.ordinal()); // Action: Update data
            tags.setInteger("questID", btn.getStoredValue().getID());
            tags.setTag("data", base);
            PacketSender.INSTANCE.sendToServer(new QuestingPacket(PacketTypeNative.QUEST_EDIT.GetLocation(), tags));
        }
		
		return true;
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
	    if(PanelToolController.selected.size() <= 0 || key != Keyboard.KEY_RETURN) return false;
     
	    boolean state = !PanelToolController.selected.get(0).getStoredValue().getValue().getProperty(NativeProps.MAIN);
	    
	    for(PanelButtonQuest b : PanelToolController.selected)
        {
            b.getStoredValue().getValue().setProperty(NativeProps.MAIN, state);
            
            NBTTagCompound base = new NBTTagCompound();
            base.setTag("config", b.getStoredValue().getValue().writeToNBT(new NBTTagCompound()));
            base.setTag("progress", b.getStoredValue().getValue().writeProgressToNBT(new NBTTagCompound(), null));
            
            NBTTagCompound tags = new NBTTagCompound();
            tags.setInteger("action", EnumPacketAction.EDIT.ordinal()); // Action: Update data
            tags.setInteger("questID", b.getStoredValue().getID());
            tags.setTag("data", base);
            PacketSender.INSTANCE.sendToServer(new QuestingPacket(PacketTypeNative.QUEST_EDIT.GetLocation(), tags));
        }
	    
        return true;
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
    
    @Override
    public boolean useSelection()
    {
        return true;
    }
}
