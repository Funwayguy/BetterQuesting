package betterquesting.client.toolbox.tools;

import betterquesting.api.client.gui.controls.GuiButtonQuestInstance;
import betterquesting.api.client.gui.misc.IGuiQuestLine;
import betterquesting.api.client.toolbox.IToolboxTool;
import betterquesting.api.enums.EnumPacketAction;
import betterquesting.api.network.QuestingPacket;
import betterquesting.api.questing.IQuest;
import betterquesting.api.questing.IQuestLine;
import betterquesting.client.toolbox.ToolboxGuiMain;
import betterquesting.network.PacketSender;
import betterquesting.network.PacketTypeNative;
import betterquesting.questing.QuestDatabase;
import betterquesting.questing.QuestInstance;
import betterquesting.questing.QuestLineDatabase;
import betterquesting.questing.QuestLineEntry;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTTagCompound;

public class ToolboxToolCopy implements IToolboxTool
{
	IGuiQuestLine gui = null;
	GuiButtonQuestInstance btnQuest = null;
	
	@Override
	public void initTool(IGuiQuestLine gui)
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
	public void drawTool(int mx, int my, float partialTick)
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
		
		btnQuest.x = mx;
		btnQuest.y = my;
		btnQuest.drawButton(Minecraft.getMinecraft(), mx, my, partialTick);
		
		ToolboxGuiMain.drawGrid(gui);
	}
	
	@Override
	public void onMouseClick(int mx, int my, int click)
	{
		if(click == 1 && btnQuest != null)
		{
			btnQuest = null;
		} else if(click != 0)
		{
			return;
		}
		
		int snap = ToolboxGuiMain.getSnapValue();
		int modX = ((mx%snap) + snap)%snap;
		int modY = ((my%snap) + snap)%snap;
		mx -= modX;
		my -= modY;
		
		if(btnQuest == null)
		{
			GuiButtonQuestInstance tmpBtn = gui.getQuestLine().getButtonAt(mx, my);
			
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
			base2.setTag("line", qLine.writeToNBT(new NBTTagCompound()));
			tag2.setTag("data", base2);
			tag2.setInteger("action", EnumPacketAction.EDIT.ordinal());
			tag2.setInteger("lineID", lID);
			PacketSender.INSTANCE.sendToServer(new QuestingPacket(PacketTypeNative.LINE_EDIT.GetLocation(), tag2));
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
		return btnQuest == null;
	}
	
	@Override
	public boolean allowScrolling(int click)
	{
		return btnQuest == null || click == 2;
	}
	
	@Override
	public boolean allowZoom()
	{
		return true;
	}
	
	@Override
	public boolean clampScrolling()
	{
		return btnQuest == null;
	}
}
