package betterquesting.client.toolbox.tools;

import betterquesting.api.client.toolbox.IToolboxTool;
import betterquesting.api2.client.gui.controls.PanelButtonQuest;
import betterquesting.client.gui2.CanvasQuestLine;
import betterquesting.client.toolbox.ToolboxGuiMain;

import java.util.Collections;
import java.util.List;

public class ToolboxToolCopy implements IToolboxTool
{
	private CanvasQuestLine gui = null;
	private PanelButtonQuest btnQuest = null;
	
	@Override
	public void initTool(CanvasQuestLine gui)
	{
		this.gui = gui;
		this.btnQuest = null;
	}
	
	@Override
	public void disableTool()
	{
		if(btnQuest != null)
		{
			btnQuest = null;
		}
	}
	
	@Override
	public void drawCanvas(int mx, int my, float partialTick)
	{
		if(btnQuest == null)
		{
			return;
		}
		
		int snap = ToolboxGuiMain.getSnapValue();
		int modX = ((mx%snap) + snap)%snap;
		int modY = ((my%snap) + snap)%snap;
		mx -= modX;
		my -= modY;
		
		btnQuest.rect.x = mx;
		btnQuest.rect.y = my;
		btnQuest.drawPanel(mx, my, partialTick); // TODO: Use relative canvas coordinates
		
		ToolboxGuiMain.drawGrid(gui);
	}
	
	@Override
    public void drawOverlay(int mx, int my, float partialTick)
    {
    }
    
    @Override
    public List<String> getTooltip(int mx, int my)
    {
        return btnQuest == null ? null : Collections.emptyList();
    }
	
	@Override
	public boolean onMouseClick(int mx, int my, int click)
	{
		if(click == 1 && btnQuest != null)
		{
			btnQuest = null;
			return true;
		} else if(click != 0)
		{
			return false;
		}
		
		// TODO
		/*int snap = ToolboxGuiMain.getSnapValue();
		int modX = ((mx%snap) + snap)%snap;
		int modY = ((my%snap) + snap)%snap;
		mx -= modX;
		my -= modY;
		
		if(btnQuest == null)
		{
			GuiButtonQuestInstance tmpBtn = gui.getButtonAt(mx, my);
			
			if(tmpBtn != null)
			{
				QuestInstance tmpQ = new QuestInstance(); // Unregistered but setup
				tmpQ.readFromNBT(tmpBtn.getQuest().writeToNBT(new NBTTagCompound()));
				btnQuest = new GuiButtonQuestInstance(0, mx, my, tmpBtn.width, tmpBtn.height, tmpQ);
			}
		} else
		{
			// Pre-sync
			IQuest quest = btnQuest.getQuest();
			IQuestLine qLine = gui.getQuestLine().getQuestLine();
			int qID = QuestDatabase.INSTANCE.nextID();
			int lID = QuestLineDatabase.INSTANCE.getID(qLine);
			if(qLine.getValue(qID) == null)
			{
				QuestLineEntry qe = new QuestLineEntry(mx, my, Math.max(btnQuest.width, btnQuest.height));
				qLine.add(qID, qe);
			}
			btnQuest = null;
			
			// Sync Quest
			NBTTagCompound tag1 = new NBTTagCompound();
			NBTTagCompound base1 = new NBTTagCompound();
			base1.setTag("config", quest.writeToNBT(new NBTTagCompound()));
			tag1.setTag("data", base1);
			tag1.setInteger("action", EnumPacketAction.ADD.ordinal());
			tag1.setInteger("questID", qID);
			PacketSender.INSTANCE.sendToServer(new QuestingPacket(PacketTypeNative.QUEST_EDIT.GetLocation(), tag1));
			
			// Sync Line
			NBTTagCompound tag2 = new NBTTagCompound();
			NBTTagCompound base2 = new NBTTagCompound();
			base2.setTag("line", qLine.writeToNBT(new NBTTagCompound(), null));
			tag2.setTag("data", base2);
			tag2.setInteger("action", EnumPacketAction.EDIT.ordinal());
			tag2.setInteger("lineID", lID);
			PacketSender.INSTANCE.sendToServer(new QuestingPacket(PacketTypeNative.LINE_EDIT.GetLocation(), tag2));
		}*/
		
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
	public boolean onKeyPressed(char c, int keyCode)
	{
	    return false;
	}
	
	@Override
	public boolean clampScrolling()
	{
		return btnQuest == null;
	}
}
