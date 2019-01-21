package betterquesting.client.toolbox.tools;

import betterquesting.api.client.toolbox.IToolboxTool;
import betterquesting.api.enums.EnumPacketAction;
import betterquesting.api.network.QuestingPacket;
import betterquesting.api.questing.IQuestLine;
import betterquesting.api.questing.IQuestLineEntry;
import betterquesting.api2.client.gui.controls.PanelButtonQuest;
import betterquesting.client.gui2.CanvasQuestLine;
import betterquesting.client.toolbox.ToolboxGuiMain;
import betterquesting.network.PacketSender;
import betterquesting.network.PacketTypeNative;
import betterquesting.questing.QuestDatabase;
import betterquesting.questing.QuestLineDatabase;
import net.minecraft.nbt.NBTTagCompound;

import java.util.Collections;
import java.util.List;

public class ToolboxToolScale implements IToolboxTool
{
	private CanvasQuestLine gui;
	private int grabID = -1;
	private PanelButtonQuest grabbed;
	
	@Override
	public void initTool(CanvasQuestLine gui)
	{
		this.gui = gui;
		grabbed = null;
		grabID = -1;
	}

	@Override
	public void disableTool()
	{
		if(grabbed != null)
		{
			IQuestLineEntry qle = gui.getQuestLine().getValue(grabID);
			
			if(qle != null)
			{
				// Reset size
				grabbed.rect.w = qle.getSize();
				grabbed.rect.h = qle.getSize();
			}
		}
		
		grabbed = null;
		grabID = -1;
	}

	@Override
	public void drawCanvas(int mx, int my, float partialTick)
	{
		if(grabbed != null)
		{
			int snap = ToolboxGuiMain.getSnapValue();
			
			int size = Math.max(mx - grabbed.rect.x, my - grabbed.rect.y);
			int mult = Math.max(1, (int)Math.ceil(size/(float)snap));
			size = mult * snap;
			
			grabbed.rect.w = size;
			grabbed.rect.h = size;
		}
		
		ToolboxGuiMain.drawGrid(gui);
	}
	
	@Override
    public void drawOverlay(int mx, int my, float partialTick)
    {
    }
    
    @Override
    public List<String> getTooltip(int mx, int my)
    {
        return grabbed == null ? null : Collections.emptyList();
    }

	@Override
	public boolean onMouseClick(int mx, int my, int click)
	{
		if(click == 1 && grabbed != null)
		{
			IQuestLineEntry qle = gui.getQuestLine().getValue(grabID);
			
			if(qle != null)
			{
				// Reset size
				grabbed.rect.w = qle.getSize();
				grabbed.rect.h = qle.getSize();
			}
			
			grabbed = null;
			grabID = -1;
			return true;
		} else if(click != 0)
		{
			return false;
		}
		
		if(grabbed == null)
		{
			grabbed = gui.getButtonAt(mx, my);
			grabID = grabbed == null? -1 : QuestDatabase.INSTANCE.getID(grabbed.getStoredValue());
			return grabID >= 0;
		} else
		{
			IQuestLine qLine = gui.getQuestLine();
			int lID = QuestLineDatabase.INSTANCE.getID(qLine);
			IQuestLineEntry qle = gui.getQuestLine().getValue(grabID);
			
			if(qle != null)
			{
				qle.setSize(Math.max(grabbed.rect.w, grabbed.rect.h));
				
				// Sync Line
				NBTTagCompound tag2 = new NBTTagCompound();
				NBTTagCompound base2 = new NBTTagCompound();
				base2.setTag("line", qLine.writeToNBT(new NBTTagCompound(), null));
				tag2.setTag("data", base2);
				tag2.setInteger("action", EnumPacketAction.EDIT.ordinal());
				tag2.setInteger("lineID", lID);
				PacketSender.INSTANCE.sendToServer(new QuestingPacket(PacketTypeNative.LINE_EDIT.GetLocation(), tag2));
			}
			
			grabbed = null;
			grabID = -1;
			
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
