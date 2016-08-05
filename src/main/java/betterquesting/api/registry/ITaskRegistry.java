package betterquesting.api.registry;

import java.util.List;
import net.minecraft.util.ResourceLocation;
import betterquesting.api.quests.tasks.ITaskBase;
import betterquesting.api.quests.tasks.ITaskFactory;

public interface ITaskRegistry
{
	public void registerTask(ITaskFactory<? extends ITaskBase> factory);
	public ITaskFactory<? extends ITaskBase> getFactory(ResourceLocation name);
	public List<ITaskFactory<? extends ITaskBase>> getAll();
	public ITaskBase createTask(ResourceLocation name);
}
