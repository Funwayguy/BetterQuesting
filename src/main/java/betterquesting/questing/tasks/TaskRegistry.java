package betterquesting.questing.tasks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import net.minecraft.util.ResourceLocation;
import org.apache.logging.log4j.Level;
import betterquesting.api.misc.IFactory;
import betterquesting.api.placeholders.tasks.FactoryTaskPlaceholder;
import betterquesting.api.questing.tasks.ITask;
import betterquesting.api.questing.tasks.ITaskRegistry;
import betterquesting.core.BetterQuesting;

/**
 * Registry for all known task types. Questing packs should register their custom types here for proper saving/loading
 */
public class TaskRegistry implements ITaskRegistry
{
	public static final TaskRegistry INSTANCE = new TaskRegistry();
	
	private HashMap<ResourceLocation, IFactory<? extends ITask>> taskRegistry = new HashMap<ResourceLocation, IFactory<? extends ITask>>();
	
	private TaskRegistry()
	{
	}
	
	@Override
	public void registerTask(IFactory<? extends ITask> factory)
	{
		if(factory == null)
		{
			throw new NullPointerException("Tried to register null task");
		} else if(factory.getRegistryName() == null)
		{
			throw new IllegalArgumentException("Tried to register a task with a null name: " + factory.getClass());
		}
		
		if(taskRegistry.containsKey(factory.getRegistryName()) || taskRegistry.containsValue(factory))
		{
			throw new IllegalArgumentException("Cannot register dupliate task type: " + factory.getRegistryName());
		}
		
		taskRegistry.put(factory.getRegistryName(), factory);
	}
	
	@Override
	public IFactory<? extends ITask> getFactory(ResourceLocation registryName)
	{
		return taskRegistry.get(registryName);
	}
	
	@Override
	public List<IFactory<? extends ITask>> getAll()
	{
		return new ArrayList<IFactory<? extends ITask>>(taskRegistry.values());
	}
	
	public ITask createTask(ResourceLocation registryName)
	{
		try
		{
			IFactory<? extends ITask> factory = null;
			
			if(FactoryTaskPlaceholder.INSTANCE.getRegistryName().equals(registryName))
			{
				factory = FactoryTaskPlaceholder.INSTANCE;
			} else
			{
				factory = getFactory(registryName);
			}
			
			if(factory == null)
			{
				BetterQuesting.logger.log(Level.ERROR, "Tried to load missing task type '" + registryName + "'! Are you missing an expansion pack?");
				return null;
			}
			
			return factory.createNew();
		} catch(Exception e)
		{
			BetterQuesting.logger.log(Level.ERROR, "Unable to instatiate task: " + registryName, e);
			return null;
		}
	}
}
