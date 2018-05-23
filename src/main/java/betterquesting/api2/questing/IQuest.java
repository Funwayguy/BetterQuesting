package betterquesting.api2.questing;

/*
Notes:
- IQuests should never be in charge of writing their own IDs to NBT. The database will look that up and write it after the quest has save its own data.
- Anything relating to the description or appearance of the quest should be stored in properties from now on.
- State changes should now be broadcasted via events. The a quest manager will deal with the syncing and notifications instead of the quest instance
- IQuests will no longer be updated on LivingUpdate unless they have been cached with a pending-autoclaim or reset. Tasks will update themselves and ping the quest when changed
- The quest cache, although periodically automatic, can and should be updated manually from now on whenever the quest state changes.
 */

import betterquesting.api.misc.INBTSaveLoad;
import betterquesting.api.properties.IPropertyContainer;
import betterquesting.api.questing.rewards.IReward;
import betterquesting.api.questing.tasks.ITask;
import betterquesting.api2.storage.IDatabase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;

import java.util.List;
import java.util.UUID;

public interface IQuest extends INBTSaveLoad<NBTTagCompound>
{
    IPropertyContainer getProperties();
    
    IDatabase<ITask> getTasks();
    IDatabase<IReward> getRewards();
    
    // Does not include task data
    IPropertyContainer getUserData(UUID uuid);
    
    void detect(EntityPlayer player);
    
    boolean canClaim(EntityPlayer player);
    void claim(EntityPlayer player);
    
    void resetUser(UUID uuid, boolean progressOnly);
    void resetAll(boolean progressOnly);
    
    List<Integer> getRequirements(); // TODO: Replace with scriptable conditions later
}