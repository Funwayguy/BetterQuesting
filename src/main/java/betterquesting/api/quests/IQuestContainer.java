package betterquesting.api.quests;

import java.util.UUID;
import javax.annotation.Nullable;
import com.google.gson.JsonObject;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import betterquesting.api.database.IJsonSaveLoad;
import betterquesting.api.database.IRegStorage;
import betterquesting.api.enums.EnumQuestState;
import betterquesting.api.enums.EnumQuestVisibility;
import betterquesting.api.quests.rewards.IRewardBase;
import betterquesting.api.quests.tasks.ITaskBase;
import betterquesting.api.utils.BigItemStack;

public interface IQuestContainer extends IJsonSaveLoad<JsonObject>
{
	public int getQuestID();
	public String getUnlocalisedName();
	public String getUnlocalisedDescription();
	
	public BigItemStack getIcon();
	public EnumQuestState getState(UUID uuid);
	public EnumQuestVisibility getVisibility();
	
	public boolean isGlobal();
	public boolean isMain();
	
	public void update(EntityPlayer player);
	public void detect(EntityPlayer player);
	
	public boolean isUnlocked(UUID uuid);
	public boolean canSubmit(EntityPlayer player);
	
	public boolean isComplete(UUID uuid);
	public void setComplete(UUID uuid, long timeStamp);
	
	public boolean canClaim(EntityPlayer player);
	public void claimReward(EntityPlayer player);
	
	public void reset(UUID uuid);
	public void resetAll();
	
	/**
	 * Returns the internal task database or null if this quest doesn't support tasks
	 */
	@Nullable
	public IRegStorage<ITaskBase> getTasks();
	
	/**
	 * Returns the internal reward database or null if this quest doesn't support rewards
	 */
	@Nullable
	public IRegStorage<IRewardBase> getRewards();
	
	public void syncAll();
	public void syncPlayer(EntityPlayerMP player);
}
