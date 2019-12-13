package betterquesting.questing.rewards;

import betterquesting.api.placeholders.rewards.RewardPlaceholder;
import betterquesting.api.questing.rewards.IReward;
import betterquesting.api2.storage.DBEntry;
import betterquesting.api2.storage.IDatabaseNBT;
import betterquesting.api2.storage.SimpleDatabase;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class RewardStorage extends SimpleDatabase<IReward> implements IDatabaseNBT<IReward, ListNBT, ListNBT>
{
	@Override
	public ListNBT writeToNBT(ListNBT json, @Nullable List<Integer> subset)
	{
		for(DBEntry<IReward> rew : getEntries())
		{
			ResourceLocation rewardID = rew.getValue().getFactoryID();
			CompoundNBT rJson = rew.getValue().writeToNBT(new CompoundNBT());
			rJson.putString("rewardID", rewardID.toString());
			rJson.putInt("index", rew.getID());
			json.add(rJson);
		}
		
		return json;
	}
	
	@Override
	public void readFromNBT(ListNBT json, boolean merge)
	{
	    reset();
		List<IReward> unassigned = new ArrayList<>();
		
		for(int i = 0; i < json.size(); i++)
		{
			CompoundNBT jsonReward = json.getCompound(i);
			ResourceLocation loc = new ResourceLocation(jsonReward.getString("rewardID"));
			int index = jsonReward.contains("index", 99) ? jsonReward.getInt("index") : -1;
			IReward reward = RewardRegistry.INSTANCE.createNew(loc);
			
			if(reward instanceof RewardPlaceholder)
			{
				CompoundNBT jr2 = jsonReward.getCompound("orig_data");
				ResourceLocation loc2 = new ResourceLocation(jr2.getString("rewardID"));
				IReward r2 = RewardRegistry.INSTANCE.createNew(loc2);
				
				if(r2 != null)
				{
					jsonReward = jr2;
					reward = r2;
				}
			}
			
			if(reward != null)
			{
				reward.readFromNBT(jsonReward);
				
				if(index >= 0)
				{
					add(index, reward);
				} else
				{
					unassigned.add(reward);
				}
			} else
			{
				RewardPlaceholder rph = new RewardPlaceholder();
				rph.setRewardConfigData(jsonReward);
				
				if(index >= 0)
				{
					add(index, rph);
				} else
				{
					unassigned.add(rph);
				}
			}
		}
		
		for(IReward r : unassigned)
		{
			add(nextID(), r);
		}
	}
	
	// === Future support ===
	
	@Override
    public ListNBT writeProgressToNBT(ListNBT nbt, @Nullable List<UUID> user)
    {
        return nbt;
    }
    
    @Override
    public void readProgressFromNBT(ListNBT nbt, boolean merge)
    {
    }
}
