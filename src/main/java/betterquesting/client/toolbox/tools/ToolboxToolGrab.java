package betterquesting.client.toolbox.tools;

import betterquesting.api.client.gui.GuiElement;
import betterquesting.api.client.gui.controls.GuiButtonQuestInstance;
import betterquesting.api.client.gui.misc.IGuiQuestLine;
import betterquesting.api.client.toolbox.IToolboxTool;
import betterquesting.api.enums.EnumPacketAction;
import betterquesting.api.network.QuestingPacket;
import betterquesting.api.questing.IQuestLine;
import betterquesting.api.questing.IQuestLineEntry;
import betterquesting.client.toolbox.ToolboxGuiMain;
import betterquesting.network.PacketSender;
import betterquesting.network.PacketTypeNative;
import betterquesting.questing.QuestDatabase;
import betterquesting.questing.QuestLineDatabase;
import net.minecraft.nbt.NBTTagCompound;

public class ToolboxToolGrab extends GuiElement implements IToolboxTool
{
	IGuiQuestLine gui;
	int grabID = -1;
	GuiButtonQuestInstance grabbed;
	
	@Override
	public void initTool(IGuiQuestLine gui)
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
			IQuestLineEntry qle = gui.getQuestLine().getQuestLine().getValue(grabID);
			
			if(qle != null)
			{
				// Reset position
				grabbed.x = qle.getPosX();
				grabbed.y = qle.getPosY();
			}
		}
		
		grabbed = null;
		grabID = -1;
	}
	
	@Override
	public void drawTool(int mx, int my, float partialTick)
	{
		if(grabbed != null)
		{
			int snap = ToolboxGuiMain.getSnapValue();
			grabbed.x = mx;
			grabbed.y = my;
			int modX = ((grabbed.x%snap) + snap)%snap;
			int modY = ((grabbed.y%snap) + snap)%snap;
			grabbed.x -= modX;
			grabbed.y -= modY;
		}
		
		ToolboxGuiMain.drawGrid(gui);
	}
	
	@Override
	public void onMouseClick(int mx, int my, int click)
	{
		if(click == 1 && grabbed != null)
		{
			IQuestLineEntry qle = gui.getQuestLine().getQuestLine().getValue(grabID);
			
			if(qle != null)
			{
				// Reset position
				grabbed.x = qle.getPosX();
				grabbed.y = qle.getPosY();
			}
			
			grabbed = null;
			return;
		} else if(click != 0)
		{
			return;
		}
		
		if(grabbed == null)
		{
			grabbed = gui.getQuestLine().getButtonAt(mx, my);
			grabID = grabbed == null? -1 : QuestDatabase.INSTANCE.getID(grabbed.getQuest());
		} else
		{
			IQuestLine qLine = gui.getQuestLine().getQuestLine();
			int lID = QuestLineDatabase.INSTANCE.getID(qLine);
			IQuestLineEntry qle = gui.getQuestLine().getQuestLine().getValue(grabID);
			
			if(qle != null)
			{
				qle.setPosition(grabbed.x, grabbed.y);
				
				// Sync Line
				NBTTagCompound tag2 = new NBTTagCompound();
				NBTTagCompound base2 = new NBTTagCompound();
				base2.setTag("line", qLine.writeToNBT(new NBTTagCompound()));
				tag2.setTag("data", base2);
				tag2.setInteger("action", EnumPacketAction.EDIT.ordinal());
				tag2.setInteger("lineID", lID);
				PacketSender.INSTANCE.sendToServer(new QuestingPacket(PacketTypeNative.LINE_EDIT.GetLocation(), tag2));
			}
			
			grabbed = null;
			grabID = -1;
		}
	}
	
	@Override
	public void onMouseScroll(int mx, int my, int scroll)
	{
	}
	
	@Override
	public void onKeyPressed(char c, int keyCode)
	{
	}
	
	@Override
	public boolean allowTooltips()
	{
		return grabbed == null;
	}
	
	@Override
	public boolean allowScrolling(int click)
	{
		return grabbed == null || click == 2;
	}
	
	@Override
	public boolean allowZoom()
	{
		return true;
	}
	
	@Override
	public boolean clampScrolling()
	{
		return false;
	}
}
