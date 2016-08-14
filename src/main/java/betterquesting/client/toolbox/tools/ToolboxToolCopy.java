package betterquesting.client.toolbox.tools;

import net.minecraft.nbt.NBTTagCompound;
import com.google.gson.JsonObject;
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

public class ToolboxToolCopy extends ToolboxTool
{
	GuiButtonQuestInstance btnQuest;
	
	public ToolboxToolCopy(GuiQuesting screen)
	{
		super(screen);
	}
	
	@Override
	public void initTool(GuiQuestLinesEmbedded ui)
	{
		super.initTool(ui);
		
		btnQuest = null;
	}
	
	@Override
	public void drawTool(int mx, int my, float partialTick)
	{
		if(btnQuest == null)
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
		
		btnQuest.xPosition = rmx;
		btnQuest.yPosition = rmy;
		
		ToolboxGuiMain.drawGrid(ui);
	}
	
	@Override
	public void onMouseClick(int mx, int my, int click)
	{
		if(!screen.isWithin(mx, my, ui.getPosX(), ui.getPosY(), ui.getWidth(), ui.getHeight()) || ui.getQuestLine() == null)
		{
			return;
		}
		
		if(click == 1 && btnQuest != null)
		{
			ui.getButtons().remove(btnQuest);
			btnQuest = null;
		} else if(click != 0)
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
		
		if(btnQuest == null)
		{
			GuiButtonQuestInstance tmpBtn = ui.getClickedQuest(mx, my);
			
			if(tmpBtn != null)
			{
				QuestInstance tmpQ = new QuestInstance(-1, false); // Unregistered but setup
				JsonObject j = new JsonObject();
				tmpBtn.quest.writeToJSON(j);
				tmpQ.readFromJSON(j);
				btnQuest = new GuiButtonQuestInstance(0, rmx, rmy, tmpQ);
				ui.getButtons().add(btnQuest);
			}
		} else
		{
			QuestLine qLine = ui.getQuestLine();
			int qID = QuestDatabase.getUniqueID();
			btnQuest.quest.questID = qID;
			QuestDatabase.questDB.put(qID, btnQuest.quest);
			QuestLineEntry qe = new QuestLineEntry(btnQuest.quest, rmx, rmy);
			qLine.questList.add(qe);
			btnQuest = null;
			
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
	}
	
	@Override
	public void deactivateTool()
	{
		if(btnQuest != null)
		{
			ui.getButtons().remove(btnQuest);
			btnQuest = null;
		}
	}
	
	@Override
	public boolean showTooltips()
	{
		return btnQuest == null;
	}
	
	@Override
	public boolean allowDragging(int click)
	{
		return btnQuest == null || click == 2;
	}
	
	@Override
	public boolean clampScrolling()
	{
		return btnQuest == null;
	}
}
