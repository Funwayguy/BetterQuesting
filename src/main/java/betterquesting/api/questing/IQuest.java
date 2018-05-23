package betterquesting.api.questing;

import java.util.List;
import java.util.UUID;

import betterquesting.api.questing.rewards.IReward;
import betterquesting.api2.storage.IDatabaseNBT;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import betterquesting.api.enums.EnumQuestState;
import betterquesting.api.misc.IDataSync;
import betterquesting.api.misc.INBTSaveLoad;
import betterquesting.api.properties.IPropertyContainer;
import betterquesting.api.questing.tasks.ITask;
import betterquesting.api.utils.BigItemStack;

public interface IQuest extends INBTSaveLoad<NBTTagCompound>, IDataSync
{
	String getUnlocalisedName();
	String getUnlocalisedDescription();
	
	// Defaults to the API if not used
	void setParentDatabase(IQuestDatabase questDB);
	
	@SideOnly(Side.CLIENT)
	List<String> getTooltip(EntityPlayer player);
	
	BigItemStack getItemIcon();
	
	IPropertyContainer getProperties();
	
	EnumQuestState getState(UUID uuid);
	
	void update(EntityPlayer player);
	void detect(EntityPlayer player);
	
	boolean isUnlocked(UUID uuid);
	boolean canSubmit(EntityPlayer player);
	
	boolean isComplete(UUID uuid);
	void setComplete(UUID uuid, long timeStamp);
	
	boolean canClaim(EntityPlayer player);
	boolean hasClaimed(UUID uuid);
	void claimReward(EntityPlayer player);
	
	void resetUser(UUID uuid, boolean fullReset);
	void resetAll(boolean fullReset);
	
	IDatabaseNBT<ITask, NBTTagList> getTasks();
	IDatabaseNBT<IReward, NBTTagList> getRewards();
	
	List<IQuest> getPrerequisites();
}
