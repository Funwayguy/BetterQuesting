package betterquesting.misc;

import betterquesting.api.enums.EnumQuestState;
import betterquesting.api.network.QuestingPacket;
import betterquesting.api.properties.IPropertyType;
import betterquesting.api.properties.NativeProps;
import betterquesting.api.questing.IQuest;
import betterquesting.api.questing.IQuestDatabase;
import betterquesting.api.questing.rewards.IReward;
import betterquesting.api.questing.tasks.ITask;
import betterquesting.api2.storage.IDatabaseNBT;
import betterquesting.storage.PropertyContainer;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
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
	private final List<String> tooltip = new ArrayList<>();
	
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
	public NBTTagCompound writeToNBT(NBTTagCompound nbt)
	{
		return nbt;
	}
	
	@Override
	public void readFromNBT(NBTTagCompound nbt)
	{
	}
	
	@Override
    public NBTTagCompound writeProgressToNBT(NBTTagCompound nbt, List<UUID> users)
    {
        return nbt;
    }
    
    @Override
    public void readProgressFromNBT(NBTTagCompound nbt, boolean merge)
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
	public List<String> getTooltip(EntityPlayer player)
	{
		return tooltip;
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
	public IDatabaseNBT<ITask, NBTTagList, NBTTagList> getTasks()
	{
		return null;
	}
	
	@Override
	public IDatabaseNBT<IReward, NBTTagList, NBTTagList> getRewards()
	{
		return null;
	}
	
	@Override
	public List<IQuest> getPrerequisites()
	{
		return null;
	}
    
    @Override
    public <T> T getProperty(IPropertyType<T> prop)
    {
        return propContainer.getProperty(prop);
    }
    
    @Override
    public <T> T getProperty(IPropertyType<T> prop, T def)
    {
        return propContainer.getProperty(prop, def);
    }
    
    @Override
    public boolean hasProperty(IPropertyType<?> prop)
    {
        return propContainer.hasProperty(prop);
    }
    
    @Override
    public <T> void setProperty(IPropertyType<T> prop, T value)
    {
        propContainer.setProperty(prop, value);
    }
    
    @Override
    public UserEntry getCompletionInfo(UUID uuid)
    {
        return null;
    }
    
    @Override
    public void setCompletionInfo(UUID uuid, NBTTagCompound tags)
    {
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
}
