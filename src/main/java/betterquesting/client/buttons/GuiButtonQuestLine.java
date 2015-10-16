package betterquesting.client.buttons;

import java.util.ArrayList;
import betterquesting.quests.QuestLine;
import betterquesting.quests.QuestLine.QuestNode;

public class GuiButtonQuestLine extends GuiButtonQuesting
{
	public QuestLine line;
	public ArrayList<GuiButtonQuestInstance> buttonTree = new ArrayList<GuiButtonQuestInstance>();
	
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
		
		for(int i = 1; i <= maxDepth; i++)
		{
			ArrayList<QuestNode> nodeList = line.questTree.GetDepth(i);
			
			for(int j = 0; j < nodeList.size(); j++)
			{
				GuiButtonQuestInstance btnQuest = new GuiButtonQuestInstance(0, (i - 1) * 150, j * 30, 100, 20, nodeList.get(j).value);
				// Add parenting
				buttonTree.add(btnQuest);
			}
		}
	}
}
