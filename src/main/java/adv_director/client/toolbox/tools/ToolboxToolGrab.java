package adv_director.client.toolbox.tools;

import net.minecraft.nbt.NBTTagCompound;
import adv_director.api.client.gui.GuiElement;
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
				grabbed.xPosition = qle.getPosX();
				grabbed.yPosition = qle.getPosY();
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
			grabbed.xPosition = mx;
			grabbed.yPosition = my;
			int modX = ((grabbed.xPosition%snap) + snap)%snap;
			int modY = ((grabbed.yPosition%snap) + snap)%snap;
			grabbed.xPosition -= modX;
			grabbed.yPosition -= modY;
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
				grabbed.xPosition = qle.getPosX();
				grabbed.yPosition = qle.getPosY();
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
				qle.setPosition(grabbed.xPosition, grabbed.yPosition);
				
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
