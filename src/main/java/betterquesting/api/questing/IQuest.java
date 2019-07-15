package betterquesting.api.questing;

import betterquesting.api.enums.EnumQuestState;
import betterquesting.api.properties.IPropertyContainer;
import betterquesting.api.questing.rewards.IReward;
import betterquesting.api.questing.tasks.ITask;
import betterquesting.api2.storage.IDatabaseNBT;
import betterquesting.api2.storage.INBTProgress;
import betterquesting.api2.storage.INBTSaveLoad;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;

public interface IQuest extends INBTSaveLoad<NBTTagCompound>, INBTProgress<NBTTagCompound>, IPropertyContainer
{
	EnumQuestState getState(UUID uuid);
	
	@Nullable
	NBTTagCompound getCompletionInfo(UUID uuid);
	void setCompletionInfo(UUID uuid, @Nullable NBTTagCompound nbt);
	
	void update(EntityPlayer player);
	void detect(EntityPlayer player);
	
	boolean isUnlocked(UUID uuid);
	boolean canSubmit(EntityPlayer player);
	
	boolean isComplete(UUID uuid);
	void setComplete(UUID uuid, long timeStamp);
	
	boolean canClaim(EntityPlayer player);
	boolean hasClaimed(UUID uuid);
	void claimReward(EntityPlayer player);
	void setClaimed(UUID uuid, long timestamp);
	
	void resetUser(@Nullable UUID uuid, boolean fullReset);
	
	IDatabaseNBT<ITask, NBTTagList, NBTTagList> getTasks();
	IDatabaseNBT<IReward, NBTTagList, NBTTagList> getRewards();
	
	@Nonnull
	int[] getRequirements();
	void setRequirements(@Nonnull int[] req);
}
