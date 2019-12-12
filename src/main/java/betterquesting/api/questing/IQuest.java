package betterquesting.api.questing;

import betterquesting.api.enums.EnumQuestState;
import betterquesting.api.properties.IPropertyContainer;
import betterquesting.api.questing.rewards.IReward;
import betterquesting.api.questing.tasks.ITask;
import betterquesting.api2.storage.IDatabaseNBT;
import betterquesting.api2.storage.INBTProgress;
import betterquesting.api2.storage.INBTSaveLoad;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;

public interface IQuest extends INBTSaveLoad<CompoundNBT>, INBTProgress<CompoundNBT>, IPropertyContainer
{
	EnumQuestState getState(UUID uuid);
	
	@Nullable
	CompoundNBT getCompletionInfo(UUID uuid);
	void setCompletionInfo(UUID uuid, @Nullable CompoundNBT nbt);
	
	void update(PlayerEntity player);
	void detect(PlayerEntity player);
	
	boolean isUnlocked(UUID uuid);
	boolean canSubmit(PlayerEntity player);
	
	boolean isComplete(UUID uuid);
	void setComplete(UUID uuid, long timeStamp);
	
	boolean canClaim(PlayerEntity player);
	boolean hasClaimed(UUID uuid);
	void claimReward(PlayerEntity player);
	void setClaimed(UUID uuid, long timestamp);
	
	void resetUser(@Nullable UUID uuid, boolean fullReset);
	
	IDatabaseNBT<ITask, ListNBT, ListNBT> getTasks();
	IDatabaseNBT<IReward, ListNBT, ListNBT> getRewards();
	
	@Nonnull
	int[] getRequirements();
	void setRequirements(@Nonnull int[] req);
}
