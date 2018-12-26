package betterquesting.api.questing.tasks;

import net.minecraft.entity.player.EntityPlayer;
import betterquesting.api.questing.IQuest;

// This is to be handled by the expansions from now on.
@Deprecated
public interface ITickableTask
{
	public void updateTask(EntityPlayer player, IQuest quest);
}
