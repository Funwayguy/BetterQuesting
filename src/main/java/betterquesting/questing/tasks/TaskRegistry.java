package betterquesting.questing.tasks;

import betterquesting.api.placeholders.tasks.FactoryTaskPlaceholder;
import betterquesting.api.questing.tasks.ITask;
import betterquesting.api2.registry.IFactoryData;
import betterquesting.api2.registry.IRegistry;
import betterquesting.core.BetterQuesting;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import org.apache.logging.log4j.Level;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Registry for all known task types. Questing packs should register their custom types here for proper saving/loading
 */
public class TaskRegistry implements IRegistry<IFactoryData<ITask, NBTTagCompound>, ITask> {
    public static final TaskRegistry INSTANCE = new TaskRegistry();

    private final HashMap<ResourceLocation, IFactoryData<ITask, NBTTagCompound>> taskRegistry = new HashMap<>();

    @Override
    public void register(IFactoryData<ITask, NBTTagCompound> factory) {
        if (factory == null) {
            throw new NullPointerException("Tried to register null task");
        } else if (factory.getRegistryName() == null) {
            throw new IllegalArgumentException("Tried to register a task with a null name: " + factory.getClass());
        }

        if (taskRegistry.containsKey(factory.getRegistryName()) || taskRegistry.containsValue(factory)) {
            throw new IllegalArgumentException("Cannot register dupliate task type: " + factory.getRegistryName());
        }

        taskRegistry.put(factory.getRegistryName(), factory);
    }

    @Override
    public IFactoryData<ITask, NBTTagCompound> getFactory(ResourceLocation registryName) {
        return taskRegistry.get(registryName);
    }

    @Override
    public List<IFactoryData<ITask, NBTTagCompound>> getAll() {
        return new ArrayList<>(taskRegistry.values());
    }

    @Override
    public ITask createNew(ResourceLocation registryName) {
        try {
            IFactoryData<? extends ITask, NBTTagCompound> factory;

            if (FactoryTaskPlaceholder.INSTANCE.getRegistryName().equals(registryName)) {
                factory = FactoryTaskPlaceholder.INSTANCE;
            } else {
                factory = getFactory(registryName);
            }

            if (factory == null) {
                BetterQuesting.logger.log(Level.ERROR, "Tried to load missing task type '" + registryName + "'! Are you missing an expansion pack?");
                return null;
            }

            return factory.createNew();
        } catch (Exception e) {
            BetterQuesting.logger.log(Level.ERROR, "Unable to instatiate task: " + registryName, e);
            return null;
        }
    }
}
