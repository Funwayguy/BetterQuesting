package betterquesting.client.toolbox.tools;

import betterquesting.api.client.toolbox.IToolboxTool;
import betterquesting.api.utils.RenderUtils;
import betterquesting.api2.client.gui.controls.PanelButtonQuest;
import betterquesting.client.gui2.CanvasQuestLine;

import java.util.Collections;
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
        return Collections.emptyList();
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
		} else if(click != 0)
		{
			return false;
		}
		
		if(b1 == null)
		{
			b1 = gui.getButtonAt(mx, my);
			return b1 != null;
		} else
		{
			/*PanelButtonQuest b2 = gui.getButtonAt(mx, my);
			
			if(b1 == b2)
			{
				b1 = null;
			} else if(b2 != null)
			{
				// LINK!
				
				if(!b2.getParents().contains(b1) && !b2.getQuest().getPrerequisites().contains(b1.getQuest()) && !b1.getParents().contains(b2) && !b1.getQuest().getPrerequisites().contains(b2.getQuest()))
				{
					b2.addParent(b1);
					b2.getQuest().getPrerequisites().add(b1.getQuest());
				} else
				{
					b2.getParents().remove(b1);
					b1.getParents().remove(b2);
					b2.getQuest().getPrerequisites().remove(b1.getQuest());
					b1.getQuest().getPrerequisites().remove(b2.getQuest());
				}
				
				// Sync Quest 1
				NBTTagCompound tag1 = new NBTTagCompound();
				NBTTagCompound base1 = new NBTTagCompound();
				base1.setTag("config", b1.getQuest().writeToNBT(new  NBTTagCompound()));
				base1.setTag("progress", b1.getQuest().writeProgressToNBT(new NBTTagCompound(), null));
				tag1.setTag("data", base1);
				tag1.setInteger("action", EnumPacketAction.EDIT.ordinal());
				tag1.setInteger("questID", QuestDatabase.INSTANCE.getID(b1.getQuest()));
				
				// Sync Quest 2
				NBTTagCompound tag2 = new NBTTagCompound();
				NBTTagCompound base2 = new NBTTagCompound();
				base2.setTag("config", b2.getQuest().writeToNBT(new NBTTagCompound()));
				base1.setTag("progress", b2.getQuest().writeProgressToNBT(new NBTTagCompound(), null));
				tag2.setTag("data", base2);
				tag2.setInteger("action", EnumPacketAction.EDIT.ordinal());
				tag2.setInteger("questID", QuestDatabase.INSTANCE.getID(b2.getQuest()));
				
				PacketSender.INSTANCE.sendToServer(new QuestingPacket(PacketTypeNative.QUEST_EDIT.GetLocation(), tag1));
				PacketSender.INSTANCE.sendToServer(new QuestingPacket(PacketTypeNative.QUEST_EDIT.GetLocation(), tag2));
				
				b1 = null;
			}*/
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
