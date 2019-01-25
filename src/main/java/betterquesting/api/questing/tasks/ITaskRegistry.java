package betterquesting.api.questing.tasks;

import betterquesting.api.misc.IFactory;
import net.minecraft.util.ResourceLocation;

import java.util.List;

public interface ITaskRegistry
{
	void registerTask(IFactory<? extends ITask> factory);
	IFactory<? extends ITask> getFactory(ResourceLocation name);
	List<IFactory<? extends ITask>> getAll();
	ITask createTask(ResourceLocation name);
}
