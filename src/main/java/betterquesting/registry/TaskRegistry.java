package betterquesting.registry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import net.minecraft.util.ResourceLocation;
import org.apache.logging.log4j.Level;
import betterquesting.api.quests.tasks.ITaskBase;
import betterquesting.api.quests.tasks.ITaskFactory;
import betterquesting.api.registry.ITaskRegistry;
import betterquesting.core.BetterQuesting;

/**
 * Registry for all known task types. Questing packs should register their custom types here for proper saving/loading
 */
public class TaskRegistry implements ITaskRegistry
{
	public static final TaskRegistry INSTANCE = new TaskRegistry();
	
	private HashMap<ResourceLocation, ITaskFactory<? extends ITaskBase>> taskRegistry = new HashMap<ResourceLocation, ITaskFactory<? extends ITaskBase>>();
	
	private TaskRegistry()
	{
	}
	
	@Override
	public void registerTask(ITaskFactory<? extends ITaskBase> factory)
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
	public ITaskFactory<? extends ITaskBase> getFactory(ResourceLocation registryName)
	{
		return taskRegistry.get(registryName);
	}
	
	@Override
	public List<ITaskFactory<? extends ITaskBase>> getAll()
	{
		return new ArrayList<ITaskFactory<? extends ITaskBase>>(taskRegistry.values());
	}
	
	public ITaskBase createTask(ResourceLocation registryName)
	{
		try
		{
			ITaskFactory<? extends ITaskBase> factory = getFactory(registryName);
			
			if(factory == null)
			{
				BetterQuesting.logger.log(Level.ERROR, "Tried to load missing task type '" + registryName + "'! Are you missing an expansion pack?");
				return null;
			}
			
			return factory.createTask();
		} catch(Exception e)
		{
			BetterQuesting.logger.log(Level.ERROR, "Unable to instatiate task: " + registryName, e);
			return null;
		}
	}
}
