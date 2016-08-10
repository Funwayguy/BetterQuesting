package betterquesting.core;

import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.Level;
import betterquesting.api.IQuestingExpansion;
import betterquesting.api.QuestExpansion;
import cpw.mods.fml.common.discovery.ASMDataTable;

public class ExpansionLoader
{
	public static final ExpansionLoader INSTANCE = new ExpansionLoader();
	
	private ArrayList<IQuestingExpansion> expansions = new ArrayList<IQuestingExpansion>();
	
	private ExpansionLoader()
	{
	}
	
	/**
	 * Performs API injections before building a list of expansion instances
	 */
	public void loadExpansions(ASMDataTable asmData)
	{
		expansions.clear();
		
		for(ASMDataTable.ASMData data : asmData.getAll(QuestExpansion.class.getCanonicalName()))
		{
			try
			{
				Class<? extends IQuestingExpansion> expClass = Class.forName(data.getClassName()).asSubclass(IQuestingExpansion.class);
				expansions.add(expClass.newInstance());
			} catch(Exception e)
			{
				BetterQuesting.logger.log(Level.INFO, "Unable to load BetterQuesting expansion: ", e);
			}
		}
	}
	
	public List<IQuestingExpansion> getAllExpansions()
	{
		return expansions;
	}
}
