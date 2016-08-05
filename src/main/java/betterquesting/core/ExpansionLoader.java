package betterquesting.core;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.Level;
import cpw.mods.fml.common.discovery.ASMDataTable;
import betterquesting.api.IQuestingAPI;
import betterquesting.api.IQuestingExpansion;
import betterquesting.api.QuestExpansion;
import betterquesting.api.InjectQuestAPI;

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
		
		for(ASMDataTable.ASMData data : asmData.getAll(InjectQuestAPI.class.getCanonicalName()))
		{
			try
			{
				Class<?> injectClass = Class.forName(data.getClassName());
				Field injectField = injectClass.getDeclaredField(data.getObjectName());
				
				if(injectField != null && (injectField.getModifiers() & Modifier.STATIC) == 1 && IQuestingAPI.class.isAssignableFrom(injectField.getType()))
				{
					injectField.setAccessible(true);
					injectField.set(null, ParentAPI.API);
				}
			} catch(Exception e)
			{
				BetterQuesting.logger.log(Level.INFO, "Unable to inject BetterQuesting API: ", e);
			}
		}
		
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
