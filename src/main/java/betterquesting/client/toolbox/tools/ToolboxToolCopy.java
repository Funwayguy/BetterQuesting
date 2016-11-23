package betterquesting.client.toolbox.tools;

import net.minecraft.nbt.NBTTagCompound;
import betterquesting.api.client.gui.controls.GuiButtonQuestInstance;
import betterquesting.api.client.gui.misc.IGuiQuestLine;
import betterquesting.api.enums.EnumPacketAction;
import betterquesting.api.enums.EnumSaveType;
import betterquesting.api.network.QuestingPacket;
import betterquesting.api.questing.IQuest;
import betterquesting.api.questing.IQuestLine;
import betterquesting.api.toolbox.IToolboxTool;
import betterquesting.api.utils.NBTConverter;
import betterquesting.database.QuestDatabase;
import betterquesting.database.QuestLineDatabase;
import betterquesting.network.PacketSender;
import betterquesting.network.PacketTypeNative;
import betterquesting.quests.QuestInstance;
import betterquesting.quests.QuestLineEntry;
import com.google.gson.JsonObject;

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
			gui.getQuestLine().getButtonTree().remove(btnQuest);
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
		
		btnQuest.xPosition = mx;
		btnQuest.yPosition = my;
		
		ToolboxGuiMain.drawGrid(gui);
	}
	
	@Override
	public void onMouseClick(int mx, int my, int click)
	{
		if(click == 1 && btnQuest != null)
		{
			gui.getQuestLine().getButtonTree().remove(btnQuest);
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
				tmpQ.readFromJson(tmpBtn.getQuest().writeToJson(new JsonObject(), EnumSaveType.CONFIG), EnumSaveType.CONFIG);
				btnQuest = new GuiButtonQuestInstance(0, mx, my, 24, 24, tmpQ);
				gui.getQuestLine().getButtonTree().add(btnQuest);
			}
		} else
		{
			// Pre-sync
			IQuest quest = btnQuest.getQuest();
			IQuestLine qLine = gui.getQuestLine().getQuestLine();
			int qID = QuestDatabase.INSTANCE.nextKey();
			int lID = QuestLineDatabase.INSTANCE.getKey(qLine);
			QuestLineEntry qe = new QuestLineEntry(mx, my, 24);
			qLine.add(qe, qID);
			btnQuest = null;
			
			// Sync Quest
			NBTTagCompound tag1 = new NBTTagCompound();
			JsonObject base1 = new JsonObject();
			base1.add("config", quest.writeToJson(new JsonObject(), EnumSaveType.CONFIG));
			tag1.setTag("data", NBTConverter.JSONtoNBT_Object(base1, new NBTTagCompound()));
			tag1.setInteger("action", EnumPacketAction.ADD.ordinal());
			tag1.setInteger("questID", qID);
			PacketSender.INSTANCE.sendToServer(new QuestingPacket(PacketTypeNative.QUEST_EDIT.GetLocation(), tag1));
			
			// Sync Line
			NBTTagCompound tag2 = new NBTTagCompound();
			JsonObject base2 = new JsonObject();
			base2.add("line", qLine.writeToJson(new JsonObject(), EnumSaveType.CONFIG));
			tag2.setTag("data", NBTConverter.JSONtoNBT_Object(base2, new NBTTagCompound()));
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
