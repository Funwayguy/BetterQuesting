package betterquesting.client.gui.misc;

import java.util.ArrayList;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import betterquesting.quests.QuestLine;
import betterquesting.quests.QuestLine.QuestNode;

@SideOnly(Side.CLIENT)
public class GuiButtonQuestLine extends GuiButtonQuesting
{
	public QuestLine line;
	public ArrayList<GuiButtonQuestInstance> buttonTree = new ArrayList<GuiButtonQuestInstance>();
	public int treeW = 0;
	public int treeH = 0;
	
	public GuiButtonQuestLine(int id, int x, int y, QuestLine line)
	{
		super(id, x, y, line.name);
		this.line = line;
		BuildButtonTree();
	}
	
	public GuiButtonQuestLine(int id, int x, int y, int width, int height, QuestLine line)
	{
		super(id, x, y, width, height, line.name);
		this.line = line;
		BuildButtonTree(); 
	}
	
	public void BuildButtonTree()
	{
		buttonTree.clear();
		
		int maxDepth = line.questTree.MaxDepth();
		int leftSide = 0;
		treeW = 0;
		treeH = 0;
		
		for(int i = 1; i <= maxDepth; i++)
		{
			ArrayList<QuestNode> nodeList = line.questTree.GetDepth(i);
			
			int n = (nodeList.size() * 32 - 24)/2;
			
			for(int j = 0; j < nodeList.size(); j++)
			{
				QuestNode node = nodeList.get(j);
				GuiButtonQuestInstance btnQuest = new GuiButtonQuestInstance(0, j * 32 - n, (i - 1) * 50, node.value);
				if(btnQuest.xPosition < leftSide)
				{
					leftSide = btnQuest.xPosition;
				}
				// Add parenting
				buttonTree.add(btnQuest);
			}
		}
		
		leftSide *= -1;
		
		// Offset origin to 0,0 and establish bounds
		for(GuiButtonQuestInstance btn : buttonTree)
		{
			btn.xPosition += leftSide;
			
			treeW = Math.max(btn.xPosition + btn.width, treeW);
			treeH = Math.max(btn.yPosition + btn.height, treeH);
		}
	}
}
