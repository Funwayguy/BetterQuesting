package betterquesting.client.toolbox.tools;

import net.minecraft.nbt.NBTTagCompound;
import betterquesting.client.gui.GuiQuestLinesEmbedded;
import betterquesting.client.gui.GuiQuesting;
import betterquesting.client.gui.misc.GuiButtonQuestInstance;
import betterquesting.client.toolbox.ToolboxTool;
import betterquesting.network.PacketAssembly;
import betterquesting.network.PacketTypeRegistry.BQPacketType;
import betterquesting.quests.QuestDatabase;
import betterquesting.quests.QuestInstance;
import betterquesting.quests.QuestLine;
import betterquesting.quests.QuestLine.QuestLineEntry;
import betterquesting.utils.NBTConverter;
import com.google.gson.JsonObject;

public class ToolboxToolNew extends ToolboxTool
{
	GuiButtonQuestInstance nQuest;
	
	public ToolboxToolNew(GuiQuesting screen)
	{
		super(screen);
	}
	
	@Override
	public void drawTool(int mx, int my, float partialTick)
	{
		if(screen.isWithin(mx, my, ui.getPosX(), ui.getPosY(), ui.getWidth(), ui.getHeight()))
		{
			int snap = ToolboxGuiMain.getSnapValue();
			int rmx = ui.getRelativeX(mx);
			int rmy = ui.getRelativeY(my);
			int modX = ((rmx%snap) + snap)%snap;
			int modY = ((rmy%snap) + snap)%snap;
			rmx -= modX;
			rmy -= modY;
			
			if(nQuest == null)
			{
				nQuest = new GuiButtonQuestInstance(0, rmx, rmy, new QuestInstance(0, false));
				ui.getButtons().add(nQuest);
			}
			
			nQuest.xPosition = rmx;
			nQuest.yPosition = rmy;
		} else
		{
			ui.getButtons().remove(nQuest);
			nQuest = null;
		}
		
		ToolboxGuiMain.drawGrid(ui);
	}
	
	@Override
	public void initTool(GuiQuestLinesEmbedded ui)
	{
		ui.getButtons().remove(nQuest);
		nQuest = null;
		
		super.initTool(ui);
	}
	
	@Override
	public void deactivateTool()
	{
		if(nQuest != null)
		{
			ui.getButtons().remove(nQuest);
			nQuest = null;
		}
	}
	
	@Override
	public void onMouseClick(int mx, int my, int click)
	{
		if(click != 0 || !screen.isWithin(mx, my, ui.getPosX(), ui.getPosY(), ui.getWidth(), ui.getHeight()) || ui.getQuestLine() == null)
		{
			return;
		}
		
		int snap = ToolboxGuiMain.getSnapValue();
		int rmx = ui.getRelativeX(mx);
		int rmy = ui.getRelativeY(my);
		int modX = ((rmx%snap) + snap)%snap;
		int modY = ((rmy%snap) + snap)%snap;
		rmx -= modX;
		rmy -= modY;
		
		QuestLine qLine = ui.getQuestLine();
		QuestInstance q = new QuestInstance(QuestDatabase.getUniqueID(), true);
		QuestLineEntry qe = new QuestLineEntry(q, rmx, rmy);
		qLine.questList.add(qe);
		
		NBTTagCompound tag = new NBTTagCompound();
		JsonObject jd = new JsonObject();
		JsonObject jp = new JsonObject();
		QuestDatabase.writeToJson(jd);
		QuestDatabase.writeToJson_Progression(jp);
		tag.setTag("Data", NBTConverter.JSONtoNBT_Object(jd, new NBTTagCompound()));
		tag.setTag("Progress", NBTConverter.JSONtoNBT_Object(jp, new NBTTagCompound()));
		tag.setInteger("action", 2);
		PacketAssembly.SendToServer(BQPacketType.QUEST_EDIT.GetLocation(), tag);
	}
	
	@Override
	public boolean showTooltips()
	{
		return false;
	}
	
	@Override
	public boolean allowDragging(int click)
	{
		return click == 2;
	}
	
	@Override
	public boolean clampScrolling()
	{
		return false;
	}
}
