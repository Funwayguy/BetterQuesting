package adv_director.client.toolbox.tools;

import net.minecraft.nbt.NBTTagCompound;
import adv_director.api.client.gui.controls.GuiButtonQuestInstance;
import adv_director.api.client.gui.misc.IGuiQuestLine;
import adv_director.api.client.toolbox.IToolboxTool;
import adv_director.api.enums.EnumPacketAction;
import adv_director.api.enums.EnumSaveType;
import adv_director.api.network.QuestingPacket;
import adv_director.api.questing.IQuestLine;
import adv_director.api.questing.IQuestLineEntry;
import adv_director.api.utils.NBTConverter;
import adv_director.client.toolbox.ToolboxGuiMain;
import adv_director.network.PacketSender;
import adv_director.network.PacketTypeNative;
import adv_director.questing.QuestDatabase;
import adv_director.questing.QuestLineDatabase;
import com.google.gson.JsonObject;

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
			
			int size = Math.max(mx - grabbed.xPosition, my - grabbed.yPosition);
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
		if(click == 1)
		{
			IQuestLineEntry qle = gui.getQuestLine().getQuestLine().getValue(grabID);
			
			if(qle != null)
			{
				// Reset size
				grabbed.width = qle.getSize();
				grabbed.height = qle.getSize();
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
				JsonObject base2 = new JsonObject();
				base2.add("line", qLine.writeToJson(new JsonObject(), EnumSaveType.CONFIG));
				tag2.setTag("data", NBTConverter.JSONtoNBT_Object(base2, new NBTTagCompound()));
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
