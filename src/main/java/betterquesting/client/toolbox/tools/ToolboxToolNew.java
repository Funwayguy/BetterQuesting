package betterquesting.client.toolbox.tools;

import betterquesting.api.client.toolbox.IToolboxTool;
import betterquesting.api.questing.IQuestLine;
import betterquesting.api.questing.IQuestLineEntry;
import betterquesting.api2.client.gui.controls.PanelButtonQuest;
import betterquesting.api2.client.gui.misc.GuiRectangle;
import betterquesting.api2.client.gui.panels.lists.CanvasQuestLine;
import betterquesting.client.toolbox.ToolboxTabMain;
import betterquesting.network.handlers.NetChapterEdit;
import betterquesting.network.handlers.NetQuestEdit;
import betterquesting.questing.QuestDatabase;
import betterquesting.questing.QuestLineDatabase;
import betterquesting.questing.QuestLineEntry;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.NonNullList;

import java.util.Collections;
import java.util.List;

public class ToolboxToolNew implements IToolboxTool
{
	private CanvasQuestLine gui = null;
	private PanelButtonQuest nQuest;
	
	@Override
	public void initTool(CanvasQuestLine gui)
	{
		this.gui = gui;
		
		nQuest = new PanelButtonQuest(new GuiRectangle(0, 0, 24, 24), -1, "", null);
	}
	
	@Override
    public void refresh(CanvasQuestLine gui)
    {
    }
	
	@Override
	public void drawCanvas(int mx, int my, float partialTick)
	{
		if(nQuest == null)
		{
			return;
		}
		
		int snap = ToolboxTabMain.INSTANCE.getSnapValue();
		int modX = ((mx%snap) + snap)%snap;
		int modY = ((my%snap) + snap)%snap;
		mx -= modX;
		my -= modY;
		
		nQuest.rect.x = mx;
		nQuest.rect.y = my;
		nQuest.drawPanel(mx, my, partialTick); // TODO: Draw relative
	}
	
	@Override
    public void drawOverlay(int mx, int my, float partialTick)
    {
        ToolboxTabMain.INSTANCE.drawGrid(gui);
    }
    
    @Override
    public List<String> getTooltip(int mx, int my)
    {
        return Collections.emptyList();
    }
	
	@Override
	public void disableTool()
	{
		if(nQuest != null) nQuest = null;
	}
	
	@Override
	public boolean onMouseClick(int mx, int my, int click)
	{
		if(click != 0 || !gui.getTransform().contains(mx, my))
		{
			return false;
		}
		
		// Pre-sync
		IQuestLine qLine = gui.getQuestLine();
		int qID = QuestDatabase.INSTANCE.nextID();
		int lID = QuestLineDatabase.INSTANCE.getID(qLine);
		IQuestLineEntry qe = qLine.getValue(qID);//new QuestLineEntry(mx, my, 24);
		
		
		if(qe == null)
		{
			qe = new QuestLineEntry(nQuest.rect.x, nQuest.rect.y, 24, 24);
			qLine.add(qID, qe);
		} else
		{
			qe.setPosition(nQuest.rect.x, nQuest.rect.y);
			qe.setSize(24, 24);
		}
		
		// Sync Quest
		CompoundNBT quPayload = new CompoundNBT();
        ListNBT qdList = new ListNBT();
        CompoundNBT qTag = new CompoundNBT();
        qTag.putInt("questID", qID);
        qdList.add(qTag);
        quPayload.put("data", qdList);
        quPayload.putInt("action", 3);
        NetQuestEdit.sendEdit(quPayload);
        
		// Sync Line
		CompoundNBT chPayload = new CompoundNBT();
        ListNBT cdList = new ListNBT();
        CompoundNBT cTag = new CompoundNBT();
        cTag.putInt("chapterID", lID);
        cTag.put("config", qLine.writeToNBT(new CompoundNBT(), null));
        cdList.add(cTag);
        chPayload.put("data", cdList);
        chPayload.putInt("action", 0);
        NetChapterEdit.sendEdit(chPayload);
		
		return true;
	}
	
	@Override
	public boolean clampScrolling()
	{
		return false;
	}
	
	@Override
    public void onSelection(NonNullList<PanelButtonQuest> buttons)
    {
    }
	
	@Override
    public boolean useSelection()
    {
        return false;
    }
}
