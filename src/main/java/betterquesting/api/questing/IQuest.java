package betterquesting.api.questing;

import betterquesting.api.enums.EnumQuestState;
import betterquesting.api.misc.IDataSync;
import betterquesting.api.properties.IPropertyContainer;
import betterquesting.api.questing.rewards.IReward;
import betterquesting.api.questing.tasks.ITask;
import betterquesting.api2.storage.IDatabaseNBT;
import betterquesting.api2.storage.INBTProgress;
import betterquesting.api2.storage.INBTSaveLoad;
import betterquesting.misc.UserEntry;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

public interface IQuest extends INBTSaveLoad<NBTTagCompound>, INBTProgress<NBTTagCompound>, IPropertyContainer, IDataSync
{
	// TODO: Make this part of the constructor so it can't be modified
	void setParentDatabase(IQuestDatabase questDB);
	
	@SideOnly(Side.CLIENT)
	List<String> getTooltip(EntityPlayer player);
	
	EnumQuestState getState(UUID uuid);
	
	@Nullable
	UserEntry getCompletionInfo(UUID uuid);
	void setCompletionInfo(UUID uuid, @Nullable NBTTagCompound nbt);
	
	void update(EntityPlayer player);
	void detect(EntityPlayer player);
	
	boolean isUnlocked(UUID uuid);
	boolean canSubmit(EntityPlayer player);
	
	boolean isComplete(UUID uuid);
	void setComplete(UUID uuid, long timeStamp);
	
	@Deprecated
	boolean canClaim(EntityPlayer player);
	@Deprecated
	boolean hasClaimed(UUID uuid);
	void claimReward(EntityPlayer player);
	
	void resetUser(UUID uuid, boolean fullReset);
	void resetAll(boolean fullReset);
	
	IDatabaseNBT<ITask, NBTTagList, NBTTagList> getTasks();
	IDatabaseNBT<IReward, NBTTagList, NBTTagList> getRewards();
	
	@Deprecated
	List<IQuest> getPrerequisites();
	//NonNullList<Integer> getRequirements();
}
