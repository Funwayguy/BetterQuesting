package betterquesting.client.toolbox.tools;

import betterquesting.api.client.gui.GuiElement;
import betterquesting.api.client.gui.premade.controls.GuiButtonQuestInstance;
import betterquesting.api.client.gui.quest.IGuiQuestLine;
import betterquesting.api.client.toolbox.IToolboxTool;
import betterquesting.api.quests.IQuestLineContainer;
import betterquesting.api.quests.IQuestLineEntry;
import betterquesting.client.gui.GuiQuestLinesEmbedded;
import betterquesting.client.gui.GuiQuesting;
import betterquesting.client.toolbox.ToolboxTool;
import betterquesting.quests.QuestLine.QuestLineEntry;

public class ToolboxToolGrab extends GuiElement implements IToolboxTool
{
	int grabID = -1;
	IQuestLineEntry grabbed = null;
	IQuestLineContainer questLine;
	
	@Override
	public void initTool(IQuestLineContainer line)
	{
		this.questLine = line;
		grabbed = null;
		grabID = -1;
	}
	
	@Override
	public void disableTool()
	{
		if(grabbed != null)
		{
			IQuestLineEntry qle = questLine.getValue(grabbed.quest.questID);
			
			if(qle != null)
			{
				// Reset position
				grabbed.xPosition = qle.posX;
				grabbed.yPosition = qle.posY;
			}
		}
		
		grabbed = null;
		grabID = -1;
	}
	
	@Override
	public void drawTool(int mx, int my, float partialTick)
	{
		int rmx = ui.getRelativeX(mx);
		int rmy = ui.getRelativeY(my);
		
		if(grabbed != null)
		{
			int snap = ToolboxGuiMain.getSnapValue();
			grabbed.xPosition = rmx;
			grabbed.yPosition = rmy;
			int modX = ((grabbed.xPosition%snap) + snap)%snap;
			int modY = ((grabbed.yPosition%snap) + snap)%snap;
			grabbed.xPosition -= modX;
			grabbed.yPosition -= modY;
		}
		
		ToolboxGuiMain.drawGrid(ui);
	}
	
	@Override
	public void onMouseClick(int mx, int my, int click)
	{
		if(!screen.isWithin(mx, my, ui.getPosX(), ui.getPosY(), ui.getWidth(), ui.getHeight()))
		{
			return;
		}
		
		if(click == 1)
		{
			QuestLineEntry qle = ui.getQuestLine().getEntryByID(grabbed.quest.questID);
			
			if(qle != null)
			{
				// Reset position
				grabbed.xPosition = qle.posX;
				grabbed.yPosition = qle.posY;
			}
			
			grabbed = null;
			return;
		} else if(click != 0)
		{
			return;
		}
		
		if(grabbed == null)
		{
			grabbed = ui.getClickedQuest(mx, my);
		} else
		{
			QuestLineEntry qle = ui.getQuestLine().getEntryByID(grabbed.quest.questID);
			
			if(qle != null)
			{
				qle.posX = grabbed.xPosition;
				qle.posY = grabbed.yPosition;
				ui.autoAlign(true);
			}
			
			grabbed = null;
		}
	}
	
	@Override
	public boolean showTooltips()
	{
		return grabbed == null;
	}
	
	@Override
	public boolean allowDragging(int click)
	{
		return grabbed == null || click == 2;
	}
}
