package betterquesting.client.toolbox.tools;

import betterquesting.api.client.toolbox.IToolboxTool;
import betterquesting.api.enums.EnumPacketAction;
import betterquesting.api.network.QuestingPacket;
import betterquesting.api.questing.IQuest;
import betterquesting.api.utils.RenderUtils;
import betterquesting.api2.client.gui.controls.PanelButtonQuest;
import betterquesting.client.gui2.CanvasQuestLine;
import betterquesting.network.PacketSender;
import betterquesting.network.PacketTypeNative;
import net.minecraft.nbt.NBTTagCompound;

import java.util.List;

public class ToolboxToolLink implements IToolboxTool
{
	private CanvasQuestLine gui;
	private PanelButtonQuest b1;
	
	@Override
	public void initTool(CanvasQuestLine gui)
	{
		this.gui = gui;
		b1 = null;
	}
	
	@Override
	public void disableTool()
	{
		b1 = null;
	}
	
	@Override
    public void refresh(CanvasQuestLine gui)
    {
        if(b1 == null) return;
        
        for(PanelButtonQuest btn : gui.getQuestButtons())
        {
            if(btn.getStoredValue().getID() == b1.getStoredValue().getID())
            {
                b1 = btn;
                return;
            }
        }
        
        b1 = null;
    }
	
	@Override
	public void drawCanvas(int mx, int my, float partialTick)
	{
		if(b1 == null)
		{
			return;
		}
		
		RenderUtils.DrawLine(b1.rect.x + b1.rect.w/2, b1.rect.y + b1.rect.h/2, mx, my, 4F, 0xFF00FF00); // TODO: Draw relative
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
		if(click == 1)
		{
		    if(b1 != null)
            {
                b1 = null;
                return true;
            }
            
            return false;
		} else if(click != 0 || !gui.getTransform().contains(mx, my))
		{
			return false;
		}
		
		if(b1 == null)
		{
			b1 = gui.getButtonAt(mx, my);
			return b1 != null;
		} else
		{
			PanelButtonQuest b2 = gui.getButtonAt(mx, my);
			
			if(b1 == b2)
			{
				b1 = null;
			} else if(b2 != null)
			{
				// LINK!
				IQuest q1 = b1.getStoredValue().getValue();
				IQuest q2 = b2.getStoredValue().getValue();
    
				// Don't have to worry about the lines anymore. The panel is getting refereshed anyway
				if(!q2.getPrerequisites().contains(q1) && !q1.getPrerequisites().contains(q2))
				{
					q2.getPrerequisites().add(q1);
				} else
				{
					q2.getPrerequisites().remove(q1);
					q1.getPrerequisites().remove(q2);
				}
				
				// Sync Quest 1
				NBTTagCompound tag1 = new NBTTagCompound();
				NBTTagCompound base1 = new NBTTagCompound();
				base1.setTag("config",q1.writeToNBT(new  NBTTagCompound()));
				base1.setTag("progress", q1.writeProgressToNBT(new NBTTagCompound(), null));
				tag1.setTag("data", base1);
				tag1.setInteger("action", EnumPacketAction.EDIT.ordinal());
				tag1.setInteger("questID", b1.getStoredValue().getID());
				
				// Sync Quest 2
				NBTTagCompound tag2 = new NBTTagCompound();
				NBTTagCompound base2 = new NBTTagCompound();
				base2.setTag("config", q2.writeToNBT(new NBTTagCompound()));
				base1.setTag("progress", q2.writeProgressToNBT(new NBTTagCompound(), null));
				tag2.setTag("data", base2);
				tag2.setInteger("action", EnumPacketAction.EDIT.ordinal());
				tag2.setInteger("questID", b2.getStoredValue().getID());
				
				PacketSender.INSTANCE.sendToServer(new QuestingPacket(PacketTypeNative.QUEST_EDIT.GetLocation(), tag1));
				PacketSender.INSTANCE.sendToServer(new QuestingPacket(PacketTypeNative.QUEST_EDIT.GetLocation(), tag2));
				
				b1 = null;
			}
			return true;
		}
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
	public boolean clampScrolling()
	{
		return true;
	}
}
