package betterquesting.misc;

import betterquesting.api.enums.EnumQuestState;
import betterquesting.api.enums.EnumSaveType;
import betterquesting.api.network.QuestingPacket;
import betterquesting.api.properties.IPropertyContainer;
import betterquesting.api.properties.IPropertyType;
import betterquesting.api.properties.NativeProps;
import betterquesting.api.questing.IQuest;
import betterquesting.api.questing.IQuestDatabase;
import betterquesting.api.questing.rewards.IReward;
import betterquesting.api.questing.tasks.ITask;
import betterquesting.api.utils.BigItemStack;
import betterquesting.api2.storage.IDatabaseNBT;
import betterquesting.storage.PropertyContainer;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DummyQuest implements IQuest
{
	private PropertyContainer propContainer = new HookedStorage();
	private final EnumQuestState qState;
	private IMainQuery mainCallback;
	private final BigItemStack icon = new BigItemStack(Items.AIR);
	private final List<String> tooltip = new ArrayList<>();
	
	/*public DummyQuest()
	{
		this(null);
	}*/
	
	public DummyQuest(EnumQuestState state)
	{
		this.qState = state;
	}
	
	public DummyQuest setMainCallback(IMainQuery callback)
	{
		this.mainCallback = callback;
		return this;
	}
	
	@Override
	public void setParentDatabase(IQuestDatabase questDB)
	{
	}
	
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound json, EnumSaveType saveType)
	{
		return json;
	}
	
	@Override
	public void readFromNBT(NBTTagCompound json, EnumSaveType saveType)
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
		return tooltip;
	}
	
	@Override
	public BigItemStack getItemIcon()
	{
		return icon;
	}
	
	@Override
	public IPropertyContainer getProperties()
	{
		return propContainer;
	}
	
	@Override
	public EnumQuestState getState(UUID uuid)
	{
		if(qState != null)
		{
			return qState;
		}
		
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
	public IDatabaseNBT<ITask, NBTTagList> getTasks()
	{
		return null;
	}
	
	@Override
	public IDatabaseNBT<IReward, NBTTagList> getRewards()
	{
		return null;
	}
	
	@Override
	public List<IQuest> getPrerequisites()
	{
		return null;
	}
	
	private class HookedStorage extends PropertyContainer
	{
		@Override
		@SuppressWarnings("unchecked")
		public <T> T getProperty(IPropertyType<T> prop, T def)
		{
			if(prop == null)
			{
				return def;
			} else if(mainCallback != null && prop == NativeProps.MAIN)
			{
				return (T)mainCallback.getMain(); // WARNING: THIS IS DANGEROUS AND ONLY FOR LEGACY USE
			}
			
			return super.getProperty(prop, def);
		}
	}
	
	public interface IMainQuery
	{
		Boolean getMain();
	}

	@Override
	public QuestingPacket getProgressSyncPacket(UUID player)
	{
		return null;
	}

	@Override
	public void notifyAllOnlineOfConfigChange() {
	}

	@Override
	public NBTTagCompound writeToJson_Progress(NBTTagCompound json, List<UUID> userFilter) {
		return json;
	}
}
