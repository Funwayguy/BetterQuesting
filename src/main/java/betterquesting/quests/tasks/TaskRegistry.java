package betterquesting.quests.tasks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import net.minecraft.util.ResourceLocation;
import org.apache.logging.log4j.Level;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.ModContainer;
import betterquesting.core.BetterQuesting;

/**
 * Registry for all known task types. Questing packs should register their custom types here for proper NBT saving/loading
 */
public class TaskRegistry
{
	static HashMap<ResourceLocation, Class<? extends TaskBase>> taskRegistry = new HashMap<ResourceLocation, Class<? extends TaskBase>>();
	
	@Deprecated
	public static void RegisterTask(Class<? extends TaskBase> task, String registryName)
	{
		try
		{
			ModContainer mod = Loader.instance().activeModContainer();
			
			if(registryName.contains(":"))
			{
				throw new IllegalArgumentException("Illegal character(s) used in reward ID name");
			} else if(mod == null)
			{
				throw new IllegalStateException("Tried to register a reward without a vialid mod instance");
			}
			
			RegisterTask(task, new ResourceLocation(mod.getModId() + ":" + registryName));
		} catch(Exception e)
		{
			BetterQuesting.logger.log(Level.ERROR, "An error occured while trying to register task", e);
		}
	}
	
	public static void RegisterTask(Class<? extends TaskBase> task, ResourceLocation registryName)
	{
		try
		{
			if(task == null)
			{
				throw new NullPointerException("Tried to register null task");
			} else if(registryName == null)
			{
				throw new IllegalArgumentException("Tried to register a task with a null name");
			}
			
			try
			{
				task.getDeclaredConstructor();
			} catch(NoSuchMethodException e)
			{
				throw new NoSuchMethodException("Task is missing a default constructor with 0 arguemnts");
			}
			
			if(taskRegistry.containsKey(registryName) || taskRegistry.containsValue(task))
			{
				throw new IllegalStateException("Cannot register dupliate task type '" + registryName + "'");
			}
			
			taskRegistry.put(registryName, task);
		} catch(Exception e)
		{
			BetterQuesting.logger.log(Level.ERROR, "An error occured while trying to register task", e);
		}
	}
	
	@Deprecated
	public static String GetID(Class<? extends TaskBase> task)
	{
		ResourceLocation loc = GetRegisteredName(task);
		return loc == null? null : loc.toString();
	}
	
	public static ResourceLocation GetRegisteredName(Class<? extends TaskBase> task)
	{
		for(Entry<ResourceLocation,Class<? extends TaskBase>> set : taskRegistry.entrySet())
		{
			if(set.getValue() == task)
			{
				return set.getKey();
			}
		}
		
		return null;
	}
	
	@Deprecated
	public static Class<? extends TaskBase> GetTask(String registryName)
	{
		if(registryName == null)
		{
			return null;
		}
		
		return GetTask(new ResourceLocation(registryName));
	}
	
	public static Class<? extends TaskBase> GetTask(ResourceLocation registryName)
	{
		return taskRegistry.get(registryName);
	}
	
	@Deprecated
	public static ArrayList<String> GetTypeList()
	{
		ArrayList<String> list = new ArrayList<String>();
		
		for(ResourceLocation loc : taskRegistry.keySet())
		{
			list.add(loc.toString());
		}
		
		return list;
	}
	
	public static ArrayList<ResourceLocation> GetNameList()
	{
		return new ArrayList<ResourceLocation>(taskRegistry.keySet());
	}
	
	@Deprecated
	public static TaskBase InstatiateTask(String registryName)
	{
		if(registryName == null)
		{
			return null;
		}
		
		return InstatiateTask(new ResourceLocation(registryName));
	}
	
	public static TaskBase InstatiateTask(ResourceLocation registryName)
	{
		try
		{
			Class<? extends TaskBase> task = GetTask(registryName);
			
			if(task == null)
			{
				BetterQuesting.logger.log(Level.ERROR, "Tried to load missing task type '" + registryName + "'! Are you missing an expansion pack?");
				return null;
			}
			
			return task.newInstance();
		} catch(Exception e)
		{
			BetterQuesting.logger.log(Level.ERROR, "Unable to instatiate task: " + registryName, e);
			return null;
		}
	}
}
