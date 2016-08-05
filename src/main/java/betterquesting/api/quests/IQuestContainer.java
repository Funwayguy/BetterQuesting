package betterquesting.api.quests;

import java.util.List;
import java.util.UUID;
import betterquesting.api.enums.EnumQuestState;
import betterquesting.api.enums.EnumQuestVisibility;
import betterquesting.api.quests.rewards.IRewardBase;
import betterquesting.api.quests.tasks.ITaskBase;
import com.google.gson.JsonObject;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;

public interface IQuestContainer
{
	public int getQuestID();
	public String getUnlocalisedName();
	
	public ItemStack getIcon();
	public EnumQuestState getState(UUID uuid);
	public EnumQuestVisibility getVisibility();
	
	public boolean isGlobal();
	public boolean isMain();
	
	public void update(EntityPlayer player);
	public void detect(EntityPlayer player);
	
	public boolean isComplete(UUID uuid);
	public void setComplete(UUID uuid, long timeStamp);
	
	public boolean canClaim(EntityPlayer player);
	public void claimReward(EntityPlayer player);
	
	public void reset(UUID uuid);
	public void resetAll();
	
	public int addTask(ITaskBase task);
	public ITaskBase getTask(int taskId);
	public int getTaskID(ITaskBase task);
	public List<ITaskBase> getAllTasks();
	
	public int addReward(IRewardBase reward);
	public IRewardBase getReward(int rewardId);
	public int getRewardID(IRewardBase reward);
	public List<IRewardBase> getAllRewards();
	
	public void syncAll();
	public void syncPlayer(EntityPlayerMP player);
	
	public JsonObject writeToJson_Config(JsonObject json);
	public void readFromJson_Config(JsonObject json);
	
	public JsonObject writesToJson_Progress(JsonObject json);
	public void readToJson_Progress(JsonObject json);
}
