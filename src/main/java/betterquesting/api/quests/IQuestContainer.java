package betterquesting.api.quests;

import java.util.List;
import java.util.UUID;
import net.minecraft.entity.player.EntityPlayer;
import betterquesting.api.database.IDataSync;
import betterquesting.api.database.IJsonSaveLoad;
import betterquesting.api.database.IRegStorage;
import betterquesting.api.enums.EnumQuestState;
import betterquesting.api.quests.properties.IQuestInfo;
import betterquesting.api.quests.rewards.IRewardBase;
import betterquesting.api.quests.tasks.ITaskBase;
import betterquesting.api.utils.BigItemStack;
import com.google.gson.JsonObject;

public interface IQuestContainer extends IJsonSaveLoad<JsonObject>, IDataSync
{
	public String getUnlocalisedName();
	public String getUnlocalisedDescription();
	
	public BigItemStack getItemIcon();
	
	public IQuestInfo getInfo();
	public IQuestSound getSounds();
	
	public EnumQuestState getState(UUID uuid);
	
	public void update(EntityPlayer player);
	public void detect(EntityPlayer player);
	
	public boolean isUnlocked(UUID uuid);
	public boolean canSubmit(EntityPlayer player);
	
	public boolean isComplete(UUID uuid);
	public void setComplete(UUID uuid, long timeStamp);
	
	public boolean canClaim(EntityPlayer player);
	public boolean hasClaimed(UUID uuid);
	public void claimReward(EntityPlayer player);
	
	public void resetUser(UUID uuid, boolean fullReset);
	public void resetAll(boolean fullReset);
	
	public IRegStorage<ITaskBase> getTasks();
	public IRegStorage<IRewardBase> getRewards();
	
	public List<IQuestContainer> getPrerequisites();
}
