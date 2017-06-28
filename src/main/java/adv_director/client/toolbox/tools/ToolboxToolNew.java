package adv_director.client.toolbox.tools;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTTagCompound;
import adv_director.api.client.gui.controls.GuiButtonQuestInstance;
import adv_director.api.client.gui.misc.IGuiQuestLine;
import adv_director.api.client.toolbox.IToolboxTool;
import adv_director.api.enums.EnumPacketAction;
import adv_director.api.enums.EnumSaveType;
import adv_director.api.network.QuestingPacket;
import adv_director.api.questing.IQuest;
import adv_director.api.questing.IQuestLine;
import adv_director.api.utils.NBTConverter;
import adv_director.client.toolbox.ToolboxGuiMain;
import adv_director.network.PacketSender;
import adv_director.network.PacketTypeNative;
import adv_director.questing.QuestDatabase;
import adv_director.questing.QuestInstance;
import adv_director.questing.QuestLineDatabase;
import adv_director.questing.QuestLineEntry;
import com.google.gson.JsonObject;

public class ToolboxToolNew implements IToolboxTool
{
	IGuiQuestLine gui = null;
	GuiButtonQuestInstance nQuest;
	
	@Override
	public void initTool(IGuiQuestLine gui)
	{
		this.gui = gui;
		
		nQuest = new GuiButtonQuestInstance(0, 0, 0, 24, 24, new QuestInstance());
	}
	
	@Override
	public void drawTool(int mx, int my, float partialTick)
	{
		if(nQuest == null)
		{
			return;
		}
		
		int snap = ToolboxGuiMain.getSnapValue();
		int modX = ((mx%snap) + snap)%snap;
		int modY = ((my%snap) + snap)%snap;
		mx -= modX;
		my -= modY;
		
		nQuest.xPosition = mx;
		nQuest.yPosition = my;
		nQuest.drawButton(Minecraft.getMinecraft(), mx, my);
		
		ToolboxGuiMain.drawGrid(gui);
	}
	
	@Override
	public void disableTool()
	{
		if(nQuest != null)
		{
			gui.getQuestLine().getButtonTree().remove(nQuest);
			nQuest = null;
		}
	}
	
	@Override
	public void onMouseClick(int mx, int my, int click)
	{
		if(click != 0)
		{
			return;
		}
		
		int snap = ToolboxGuiMain.getSnapValue();
		int modX = ((mx%snap) + snap)%snap;
		int modY = ((my%snap) + snap)%snap;
		mx -= modX;
		my -= modY;
		
		// Pre-sync
		IQuestLine qLine = gui.getQuestLine().getQuestLine();
		IQuest quest = new QuestInstance();
		int qID = QuestDatabase.INSTANCE.nextKey();
		int lID = QuestLineDatabase.INSTANCE.getKey(qLine);
		QuestLineEntry qe = new QuestLineEntry(mx, my, 24);
		qLine.add(qe, qID);
		
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
		return false;
	}
	
	@Override
	public boolean allowScrolling(int click)
	{
		return click == 2;
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
