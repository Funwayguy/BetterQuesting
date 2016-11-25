package betterquesting.api.questing.tasks;

import java.util.List;
import net.minecraft.util.ResourceLocation;
import betterquesting.api.misc.IFactory;

public interface ITaskRegistry
{
	public void registerTask(IFactory<ITask> factory);
	public IFactory<ITask> getFactory(ResourceLocation name);
	public List<IFactory<ITask>> getAll();
	public ITask createTask(ResourceLocation name);
}
