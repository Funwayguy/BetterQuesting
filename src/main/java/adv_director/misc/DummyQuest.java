package adv_director.misc;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.nbt.NBTTagCompound;
import adv_director.api.enums.EnumQuestState;
import adv_director.api.enums.EnumSaveType;
import adv_director.api.network.QuestingPacket;
import adv_director.api.properties.IPropertyContainer;
import adv_director.api.questing.IQuest;
import adv_director.api.questing.IQuestDatabase;
import adv_director.api.questing.rewards.IReward;
import adv_director.api.questing.tasks.ITask;
import adv_director.api.storage.IRegStorageBase;
import adv_director.api.utils.BigItemStack;
import adv_director.storage.PropertyContainer;
import com.google.gson.JsonObject;

public class DummyQuest implements IQuest
{
	public static DummyQuest dummyQuest = new DummyQuest();
	public static UUID dummyID = UUID.randomUUID();
	
	private PropertyContainer propContainer = new PropertyContainer();
	
	@Override
	public void setParentDatabase(IQuestDatabase questDB)
	{
	}
	
	@Override
	public JsonObject writeToJson(JsonObject json, EnumSaveType saveType)
	{
		return json;
	}
	
	@Override
	public void readFromJson(JsonObject json, EnumSaveType saveType)
	{
	}
	
	@Override
	public QuestingPacket getSyncPacket()
	{
		return null;
	}
	
	@Override
	public void readPacket(NBTTagCompound payload)
	{
	}
	
	@Override
	public String getUnlocalisedName()
	{
		return "Dummy";
	}
	
	@Override
	public String getUnlocalisedDescription()
	{
		return "Dummy";
	}
	
	@Override
	public List<String> getTooltip(EntityPlayer player)
	{
		return new ArrayList<String>();
	}
	
	@Override
	public BigItemStack getItemIcon()
	{
		return new BigItemStack(Items.NETHER_STAR);
	}
	
	@Override
	public IPropertyContainer getProperties()
	{
		return propContainer;
	}
	
	@Override
	public EnumQuestState getState(UUID uuid)
	{
		int state = (int)(Minecraft.getSystemTime()/1000)%4;
		
		switch(state)
		{
			case 0:
				return EnumQuestState.LOCKED;
			case 1:
				return EnumQuestState.UNLOCKED;
			case 2:
				return EnumQuestState.UNCLAIMED;
			case 3:
				return EnumQuestState.COMPLETED;
		}
		
		return EnumQuestState.COMPLETED;
	}
	
	@Override
	public void update(EntityPlayer player)
	{
	}
	
	@Override
	public void detect(EntityPlayer player)
	{
	}
	
	@Override
	public boolean isUnlocked(UUID uuid)
	{
		return false;
	}
	
	@Override
	public boolean canSubmit(EntityPlayer player)
	{
		return false;
	}
	
	@Override
	public boolean isComplete(UUID uuid)
	{
		return false;
	}
	
	@Override
	public void setComplete(UUID uuid, long timeStamp)
	{
	}
	
	@Override
	public boolean canClaim(EntityPlayer player)
	{
		return false;
	}
	
	@Override
	public boolean hasClaimed(UUID uuid)
	{
		return false;
	}
	
	@Override
	public void claimReward(EntityPlayer player)
	{
	}
	
	@Override
	public void resetUser(UUID uuid, boolean fullReset)
	{
	}
	
	@Override
	public void resetAll(boolean fullReset)
	{
	}
	
	@Override
	public IRegStorageBase<Integer,ITask> getTasks()
	{
		return null;
	}
	
	@Override
	public IRegStorageBase<Integer,IReward> getRewards()
	{
		return null;
	}
	
	@Override
	public List<IQuest> getPrerequisites()
	{
		return null;
	}
	
}
