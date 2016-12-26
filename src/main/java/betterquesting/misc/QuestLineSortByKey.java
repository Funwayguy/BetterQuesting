package betterquesting.misc;

import java.util.Comparator;
import betterquesting.api.questing.IQuestLineDatabase;

public class QuestLineSortByKey implements Comparator<Integer>
{
	private final IQuestLineDatabase parentDB;
	
	public QuestLineSortByKey(IQuestLineDatabase parentDB)
	{
		this.parentDB = parentDB;
	}
	
	@Override
	public int compare(Integer ql1, Integer ql2)
	{
		int id1 = ql1 == null? -1 : ql1;
		int id2 = ql2 == null? -1 : ql2;
		
		return (int)Math.signum(parentDB.getOrderIndex(id1) - parentDB.getOrderIndex(id2));
	}
}
