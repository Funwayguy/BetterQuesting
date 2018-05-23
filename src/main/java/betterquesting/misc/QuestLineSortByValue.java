package betterquesting.misc;

import java.util.Comparator;
import betterquesting.api.questing.IQuestLine;
import betterquesting.api.questing.IQuestLineDatabase;

public class QuestLineSortByValue implements Comparator<IQuestLine>
{
	private final IQuestLineDatabase parentDB;
	
	public QuestLineSortByValue(IQuestLineDatabase parentDB)
	{
		this.parentDB = parentDB;
	}
	
	@Override
	public int compare(IQuestLine ql1, IQuestLine ql2)
	{
		int id1 = ql1 == null? -1 : parentDB.getID(ql1);
		int id2 = ql2 == null? -1 : parentDB.getID(ql2);
		
		return (int)Math.signum(parentDB.getOrderIndex(id1) - parentDB.getOrderIndex(id2));
	}
}
