package betterquesting.quests.tasks;

import java.util.ArrayList;
import java.util.HashMap;
import org.apache.logging.log4j.Level;
import betterquesting.core.BetterQuesting;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.ModContainer;

/**
 * Registry for all known task types. Questing packs should register their custom types here for proper NBT saving/loading
 */
public class TaskRegistry
{
	static HashMap<String, Class<? extends TaskBase>> questRegistry = new HashMap<String, Class<? extends TaskBase>>();
	
	public static void RegisterTask(Class<? extends TaskBase> quest, String idName)
	{
		ModContainer mod = Loader.instance().activeModContainer();
		
		try
		{
			if(quest == null)
			{
				throw new NullPointerException("Tried to register null task");
			} else if(mod == null)
			{
				throw new IllegalArgumentException("Tried to register a task without an active mod instance");
			}
			
			try
			{
				quest.getDeclaredConstructor();
			} catch(NoSuchMethodException e)
			{
				throw new NoSuchMethodException("Task is missing a default constructor with 0 arguemnts");
			}
			
			String fullName = mod.getModId() + ":" + idName;
			
			if(questRegistry.containsKey(fullName))
			{
				throw new IllegalStateException("Cannot register dupliate task type '" + fullName + "'");
			}
			
			questRegistry.put(fullName, quest);
		} catch(Exception e)
		{
			BetterQuesting.logger.log(Level.ERROR, "An error occured while trying to register task", e);
		}
	}
	
	public static String GetID(Class<? extends TaskBase> quest)
	{
		for(String idName : questRegistry.keySet())
		{
			if(questRegistry.get(idName) == quest)
			{
				return idName;
			}
		}
		
		return null;
	}
	
	public static Class<? extends TaskBase> GetTask(String idName)
	{
		return questRegistry.get(idName);
	}
	
	public static ArrayList<String> GetTypeList()
	{
		return new ArrayList<String>(questRegistry.keySet());
	}
	
	public static TaskBase InstatiateTask(String idName)
	{
		try
		{
			Class<? extends TaskBase> task = questRegistry.get(idName);
			
			if(task == null)
			{
				BetterQuesting.logger.log(Level.ERROR, "Tried to load missing task type '" + idName + "'! Are you missing a task pack?");
				return null;
			}
			
			return questRegistry.get(idName).newInstance();
		} catch(Exception e)
		{
			BetterQuesting.logger.log(Level.ERROR, "Unable to instatiate quest: " + idName, e);
			return null;
		}
	}
}
