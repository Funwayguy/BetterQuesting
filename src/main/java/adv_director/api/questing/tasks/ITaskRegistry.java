package adv_director.api.questing.tasks;

import java.util.List;
import net.minecraft.util.ResourceLocation;
import adv_director.api.misc.IFactory;

public interface ITaskRegistry
{
	public void registerTask(IFactory<? extends ITask> factory);
	public IFactory<? extends ITask> getFactory(ResourceLocation name);
	public List<IFactory<? extends ITask>> getAll();
	public ITask createTask(ResourceLocation name);
}
