package betterquesting.client.gui.misc;

import java.util.ArrayList;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import betterquesting.quests.QuestLine;
import betterquesting.quests.QuestLine.QuestLineEntry;

@SideOnly(Side.CLIENT)
public class QuestLineButtonTree
{
	public QuestLine line;
	public ArrayList<GuiButtonQuestInstance> buttonTree = new ArrayList<GuiButtonQuestInstance>();
	public int treeW = 0;
	public int treeH = 0;
	
	public QuestLineButtonTree(QuestLine line)
	{
		this.line = line;
		BuildButtonTree();
	}
	
	public void BuildButtonTree()
	{
		buttonTree.clear();
		treeW = 0;
		treeH = 0;
		
		if(line == null)
		{
			return;
		}
		
		for(QuestLineEntry entry : line.questList)
		{
			buttonTree.add(new GuiButtonQuestInstance(0, entry.posX, entry.posY, entry.quest));
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
				if(b2 == null || btn == b2)
				{
					continue;
				}
				
				if(btn.quest.preRequisites.contains(b2.quest))
				{
					btn.parents.add(b2);
				}
			}
		}
	}
}
