package adv_director.api.questing.tasks;

import net.minecraft.entity.player.EntityPlayer;
import adv_director.api.questing.IQuest;

public interface ITickableTask
{
	public void updateTask(EntityPlayer player, IQuest quest);
}
