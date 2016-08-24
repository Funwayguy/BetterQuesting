package betterquesting.api.client.gui;

import java.util.ArrayList;
import java.util.List;
import betterquesting.api.ExpansionAPI;
import betterquesting.api.client.gui.premade.controls.GuiButtonQuestInstance;
import betterquesting.api.quests.IQuestContainer;
import betterquesting.api.quests.IQuestLineContainer;
import betterquesting.api.quests.IQuestLineEntry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Builds a tree of connected buttons based on the given quest line.
 * Intended for use in the within the draggable quest line GUI.<br>
 * <b>WARNING:</b> Button IDs will all be initialized as 0
 */
@SideOnly(Side.CLIENT)
public class QuestLineButtonTree
{
	private IQuestLineContainer line;
	private ArrayList<GuiButtonQuestInstance> buttonTree = new ArrayList<GuiButtonQuestInstance>();
	private int treeW = 0;
	private int treeH = 0;
	
	public QuestLineButtonTree(IQuestLineContainer line)
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
	
	public IQuestLineContainer getQuestLine()
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
		
		int id = line.getQuestAt(x, y);
		IQuestContainer quest = ExpansionAPI.INSTANCE.getQuestDB().getValue(id);
		
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
		
		for(int id : line.getAllKeys())
		{
			IQuestContainer quest = ExpansionAPI.INSTANCE.getQuestDB().getValue(id);
			IQuestLineEntry entry = line.getValue(id);
			
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
			
			treeW = Math.max(btn.xPosition + btn.width, treeW);
			treeH = Math.max(btn.yPosition + btn.height, treeH);
			
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
