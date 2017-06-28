package adv_director.api.questing;

import java.util.List;
import java.util.UUID;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import adv_director.api.enums.EnumQuestState;
import adv_director.api.misc.IDataSync;
import adv_director.api.misc.IJsonSaveLoad;
import adv_director.api.properties.IPropertyContainer;
import adv_director.api.questing.rewards.IReward;
import adv_director.api.questing.tasks.ITask;
import adv_director.api.storage.IRegStorageBase;
import adv_director.api.utils.BigItemStack;
import com.google.gson.JsonObject;

public interface IQuest extends IJsonSaveLoad<JsonObject>, IDataSync
{
	public String getUnlocalisedName();
	public String getUnlocalisedDescription();
	
	// Defaults to the API if not used
	public void setParentDatabase(IQuestDatabase questDB);
	
	@SideOnly(Side.CLIENT)
	public List<String> getTooltip(EntityPlayer player);
	
	public BigItemStack getItemIcon();
	
	public IPropertyContainer getProperties();
	
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
	
	public IRegStorageBase<Integer,ITask> getTasks();
	public IRegStorageBase<Integer,IReward> getRewards();
	
	public List<IQuest> getPrerequisites();
}
