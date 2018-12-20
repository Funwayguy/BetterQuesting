package betterquesting.api.client.gui;

import betterquesting.api.api.ApiReference;
import betterquesting.api.api.QuestingAPI;
import betterquesting.api.client.gui.controls.GuiButtonQuestInstance;
import betterquesting.api.questing.IQuest;
import betterquesting.api.questing.IQuestLine;
import betterquesting.api.questing.IQuestLineEntry;
import betterquesting.api2.storage.DBEntry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.List;

/**
 * Builds a tree of connected buttons based on the given quest line.
 * Intended for use in the within the draggable quest line GUI.<br>
 * <b>WARNING:</b> Button IDs will all be initialized as 0
 */
@Deprecated
@SideOnly(Side.CLIENT)
public class QuestLineButtonTree
{
	private IQuestLine line;
	private ArrayList<GuiButtonQuestInstance> buttonTree = new ArrayList<GuiButtonQuestInstance>();
	private int treeW = 0;
	private int treeH = 0;
	
	public QuestLineButtonTree(IQuestLine line)
	{
		this.line = line;
		RebuildTree();
	}
	
	public int getWidth()
	{
		return treeW;
	}
	
	public int getHeight()
	{
		return treeH;
	}
	
	public IQuestLine getQuestLine()
	{
		return line;
	}
	
	public List<GuiButtonQuestInstance> getButtonTree()
	{
		return buttonTree;
	}
	
	public GuiButtonQuestInstance getButtonAt(int x, int y)
	{
		if(line == null)
		{
			return null;
		}
		
		DBEntry<IQuestLineEntry> entry = line.getEntryAt(x, y);
		IQuest quest = entry == null ? null : QuestingAPI.getAPI(ApiReference.QUEST_DB).getValue(entry.getID());
		
		if(quest == null)
		{
			return null;
		}
		
		for(GuiButtonQuestInstance btn : buttonTree)
		{
			if(btn.getQuest() == quest)
			{
				return btn;
			}
		}
		
		return null;
	}
	
	public void RebuildTree()
	{
		buttonTree.clear();
		treeW = 0;
		treeH = 0;
		
		if(line == null)
		{
			return;
		}
		
		for(DBEntry<IQuestLineEntry> qle : line.getEntries())
		{
			IQuest quest = QuestingAPI.getAPI(ApiReference.QUEST_DB).getValue(qle.getID());
			IQuestLineEntry entry = qle.getValue();
			
			if(quest != null && entry != null)
			{
				buttonTree.add(new GuiButtonQuestInstance(0, entry.getPosX(), entry.getPosY(), entry.getSize(), entry.getSize(), quest));
			}
		}
		
		// Offset origin to 0,0 and establish bounds
		for(GuiButtonQuestInstance btn : buttonTree)
		{
			if(btn == null)
			{
				continue;
			}
			
			treeW = Math.max(btn.x + btn.width, treeW);
			treeH = Math.max(btn.y + btn.height, treeH);
			
			for(GuiButtonQuestInstance b2 : buttonTree)
			{
				if(b2 == null || btn == b2 || btn.getQuest() == null)
				{
					continue;
				}
				
				if(btn.getQuest().getPrerequisites().contains(b2.getQuest()))
				{
					btn.addParent(b2);
				}
			}
		}
	}
}
