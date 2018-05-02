package betterquesting.client.toolbox.tools;

import net.minecraft.nbt.NBTTagCompound;
import betterquesting.api.client.gui.controls.GuiButtonQuestInstance;
import betterquesting.api.client.gui.misc.IGuiQuestLine;
import betterquesting.api.client.toolbox.IToolboxTool;
import betterquesting.api.enums.EnumPacketAction;
import betterquesting.api.enums.EnumSaveType;
import betterquesting.api.network.QuestingPacket;
import betterquesting.api.questing.IQuestLine;
import betterquesting.api.questing.IQuestLineEntry;
import betterquesting.client.toolbox.ToolboxGuiMain;
import betterquesting.network.PacketSender;
import betterquesting.network.PacketTypeNative;
import betterquesting.questing.QuestDatabase;
import betterquesting.questing.QuestLineDatabase;

public class ToolboxToolScale implements IToolboxTool
{
	private IGuiQuestLine gui;
	private int grabID = -1;
	private GuiButtonQuestInstance grabbed;
	
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
				// Reset size
				grabbed.width = qle.getSize();
				grabbed.height = qle.getSize();
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
			
			int size = Math.max(mx - grabbed.x, my - grabbed.y);
			int mult = Math.max(1, (int)Math.ceil(size/(float)snap));
			size = mult * snap;
			
			grabbed.width = size;
			grabbed.height = size;
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
				// Reset size
				grabbed.width = qle.getSize();
				grabbed.height = qle.getSize();
			}
			
			grabbed = null;
			grabID = -1;
			return;
		} else if(click != 0)
		{
			return;
		}
		
		if(grabbed == null)
		{
			grabbed = gui.getQuestLine().getButtonAt(mx, my);
			grabID = grabbed == null? -1 : QuestDatabase.INSTANCE.getKey(grabbed.getQuest());
		} else
		{
			IQuestLine qLine = gui.getQuestLine().getQuestLine();
			int lID = QuestLineDatabase.INSTANCE.getKey(qLine);
			IQuestLineEntry qle = gui.getQuestLine().getQuestLine().getValue(grabID);
			
			if(qle != null)
			{
				qle.setSize(Math.max(grabbed.width, grabbed.height));
				
				// Sync Line
				NBTTagCompound tag2 = new NBTTagCompound();
				NBTTagCompound base2 = new NBTTagCompound();
				base2.setTag("line", qLine.writeToNBT(new NBTTagCompound(), EnumSaveType.CONFIG));
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
	public void onKeyPressed(char c, int key)
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
		return true;
	}
	
}
