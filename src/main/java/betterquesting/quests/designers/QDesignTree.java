package betterquesting.quests.designers;

import java.util.ArrayList;
import betterquesting.quests.QuestInstance;
import betterquesting.quests.QuestLine;
import betterquesting.quests.QuestLine.QuestLineEntry;

public class QDesignTree extends QDesign
{
	public static QDesignTree instance = new QDesignTree();
	
	@Override
	public String unlocalisedName()
	{
		return "betterquesting.design.tree";
	}
	
	@Override
	public void arrangeQuests(QuestLine line)
	{
		QuestNode questTree = new QuestNode(null);
		BuildSubTree(questTree, new ArrayList<QuestLineEntry>(line.questList));
		
		int maxDepth = questTree.MaxDepth();
		int leftSide = 0;
		
		for(int i = 1; i <= maxDepth; i++)
		{
			ArrayList<QuestNode> nodeList = questTree.GetDepth(i);
			
			int n = (nodeList.size() * 32 - 24)/2;
			
			for(int j = 0; j < nodeList.size(); j++)
			{
				QuestNode node = nodeList.get(j);
				
				node.value.posX = j * 32 - n;
				node.value.posY = (i - 1) * 48;
				
				if(node.value.posX < leftSide)
				{
					leftSide = node.value.posX;
				}
			}
		}
		
		leftSide *= -1;
		
		// Offset tree center so that the far left is lined up with 0
		for(int i = 1; i <= maxDepth; i++)
		{
			ArrayList<QuestNode> nodeList = questTree.GetDepth(i);
			
			for(int j = 0; j < nodeList.size(); j++)
			{
				QuestNode node = nodeList.get(j);
				node.value.posX += leftSide;
			}
		}
	}
	
	private void BuildSubTree(QuestNode parent, ArrayList<QuestLineEntry> pool)
	{
		ArrayList<QuestLineEntry> dependents;
		
		if(parent.value == null || parent.value.quest == null)
		{
			dependents = GetDependents(null, pool);
		} else
		{
			dependents = GetDependents(parent.value.quest, pool);
		}
		
		for(QuestLineEntry quest : dependents)
		{
			pool.remove(quest);
			QuestNode node = new QuestNode(quest);
			parent.AddChild(node);
			BuildSubTree(node, pool);
		}
		
		parent.RefreshSize();
	}
	
	private ArrayList<QuestLineEntry> GetDependents(QuestInstance quest, ArrayList<QuestLineEntry> pool)
	{
		ArrayList<QuestLineEntry> dependents = new ArrayList<QuestLineEntry>();
		
		topLoop:
		for(QuestLineEntry q : pool) // The quest line has only been permitted to show this set of quests
		{
			if(q == null || q.quest == null || dependents.contains(q))
			{
				continue;
			}
			
			if(q.quest.preRequisites.size() <= 0)
			{
				if(quest == null)
				{
					dependents.add(q);
				}
			} else if(quest != null)
			{
				if(q.quest.preRequisites.contains(quest))
				{
					dependents.add(q);
				}
			} else
			{
				for(QuestInstance r : q.quest.preRequisites)
				{
					for(QuestLineEntry qle : pool)
					{
						if(qle.quest == r)
						{
							continue topLoop;
						}
					}
				}
				
				dependents.add(q);
			}
		}
		pool.removeAll(dependents);
		return dependents;
	}
	
	private static class QuestNode
	{
		public final QuestLineEntry value;
		ArrayList<QuestNode> children = new ArrayList<QuestNode>();
		int size = 1;
		int depth = 0;
		
		public QuestNode(QuestLineEntry quest)
		{
			this.value = quest;
		}
		
		public void AddChild(QuestNode child)
		{
			children.add(child);
			child.depth = depth + 1;
		}
		
		public int MaxDepth()
		{
			if(children.size() <= 0)
			{
				return depth;
			} else
			{
				int maxDepth = depth;
				
				for(QuestNode node : children)
				{
					int d = node.MaxDepth();
					
					if(d > maxDepth)
					{
						maxDepth = d;
					}
				}
				
				return maxDepth;
			}
		}
		
		public ArrayList<QuestNode> GetDepth(int d)
		{
			ArrayList<QuestNode> list = new ArrayList<QuestNode>();
			
			if(depth == d)
			{
				if(!list.contains(this))
				{
					list.add(this);
				}
			} else if(depth < d)
			{
				for(QuestNode node : children)
				{
					list.addAll(node.GetDepth(d));
				}
			}
			
			return list;
		}
		
		public void RefreshSize()
		{
			size = 0;
			
			for(QuestNode node : children)
			{
				size += node.GetSize();
			}
			
			size = Math.max(1, size);
		}
		
		public int GetSize()
		{
			return size;
		}
	}
}
