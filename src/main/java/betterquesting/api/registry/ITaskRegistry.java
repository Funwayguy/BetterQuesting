package betterquesting.api.registry;

import java.util.List;
import net.minecraft.util.ResourceLocation;
import betterquesting.api.quests.tasks.ITask;
import betterquesting.api.utils.IFactory;

public interface ITaskRegistry
{
	public void registerTask(IFactory<ITask> factory);
	public IFactory<ITask> getFactory(ResourceLocation name);
	public List<IFactory<ITask>> getAll();
	public ITask createTask(ResourceLocation name);
}
