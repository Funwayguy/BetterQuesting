package betterquesting.api.questing.tasks;

import net.minecraft.entity.player.EntityPlayer;
import betterquesting.api.questing.IQuest;

public interface ITickableTask
{
	public void updateTask(EntityPlayer player, IQuest quest);
}
