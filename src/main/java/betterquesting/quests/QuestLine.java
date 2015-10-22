package betterquesting.quests;

import java.util.ArrayList;
import org.apache.logging.log4j.Level;
import betterquesting.core.BetterQuesting;
import betterquesting.utils.JsonHelper;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

public class QuestLine
{
	public String name = "New Quest Line";
	public ArrayList<QuestInstance> questList = new ArrayList<QuestInstance>();
	public QuestNode questTree = new QuestNode(null);
	
	public void BuildTree()
	{
		questTree = new QuestNode(null);
		
		BuildSubTree(questTree, questList);
	}
	
	private void BuildSubTree(QuestNode parent, ArrayList<QuestInstance> pool)
	{
		ArrayList<QuestInstance> dependents = GetDependents(parent.value, pool);
		ArrayList<QuestInstance> remaining = new ArrayList<QuestInstance>(pool);
		remaining.removeAll(dependents);
		
		for(QuestInstance quest : dependents)
		{
			QuestNode node = new QuestNode(quest);
			parent.AddChild(node);
			BuildSubTree(node, remaining);
		}
		
		parent.RefreshSize();
	}
	
	public ArrayList<QuestInstance> GetDependents(QuestInstance quest, ArrayList<QuestInstance> pool)
	{
		ArrayList<QuestInstance> dependents = new ArrayList<QuestInstance>();
		
		topLoop:
		for(QuestInstance q : pool) // The quest line has only been permitted to show this set of quests
		{
			if(q == null)
			{
				continue;
			}
			
			if(q.preRequisites.size() <= 0)
			{
				if(quest == null)
				{
					dependents.add(q);
				}
			} else if(quest != null)
			{
				if(q.preRequisites.contains(quest))
				{
					dependents.add(q);
				}
			} else
			{
				for(QuestInstance r : q.preRequisites)
				{
					if(pool.contains(r))
					{
						continue topLoop;
					}
				}
				
				dependents.add(q);
			}
		}
		
		return dependents;
	}
	
	public static class QuestNode
	{
		public final QuestInstance value;
		ArrayList<QuestNode> children = new ArrayList<QuestNode>();
		int size = 1;
		int depth = 0;
		
		public QuestNode(QuestInstance quest)
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
				list.add(this);
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
	
	public void writeToJSON(JsonObject json)
	{
		json.addProperty("name", name);
		
		JsonArray jArr = new JsonArray();
		
		for(QuestInstance quest : questList)
		{
			jArr.add(new JsonPrimitive(quest.questID));
		}
		
		json.add("quests", jArr);
	}
	
	public void readFromJSON(JsonObject json)
	{
		name = JsonHelper.GetString(json, "name", "New Quest Line");
		
		questList.clear();
		for(JsonElement entry : JsonHelper.GetArray(json, "quests"))
		{
			if(entry == null || !entry.isJsonPrimitive() || !entry.getAsJsonPrimitive().isNumber())
			{
				continue;
			}
			
			QuestInstance quest = QuestDatabase.getQuest(entry.getAsInt());
			
			if(quest != null)
			{
				questList.add(quest);
			} else
			{
				BetterQuesting.logger.log(Level.ERROR, "Quest line '" + this.name + "' contained an invalid quest ID: " + entry.getAsInt());
			}
		}
		
		BuildTree();
	}
}
