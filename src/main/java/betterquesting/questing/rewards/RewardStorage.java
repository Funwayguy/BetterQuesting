package betterquesting.questing.rewards;

import betterquesting.api.placeholders.rewards.RewardPlaceholder;
import betterquesting.api.questing.rewards.IReward;
import betterquesting.api2.storage.DBEntry;
import betterquesting.api2.storage.IDatabaseNBT;
import betterquesting.api2.storage.SimpleDatabase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class RewardStorage extends SimpleDatabase<IReward> implements IDatabaseNBT<IReward, NBTTagList, NBTTagList> {
    @Override
    public NBTTagList writeToNBT(NBTTagList json, @Nullable List<Integer> subset) {
        for (DBEntry<IReward> rew : getEntries()) {
            if (subset != null && !subset.contains(rew.getID())) continue;
            ResourceLocation rewardID = rew.getValue().getFactoryID();
            NBTTagCompound rJson = rew.getValue().writeToNBT(new NBTTagCompound());
            rJson.setString("rewardID", rewardID.toString());
            rJson.setInteger("index", rew.getID());
            json.appendTag(rJson);
        }

        return json;
    }

    @Override
    public void readFromNBT(NBTTagList json, boolean merge) {
        if (!merge) reset();
        List<IReward> unassigned = new ArrayList<>();

        for (int i = 0; i < json.tagCount(); i++) {
            NBTTagCompound jsonReward = json.getCompoundTagAt(i);
            ResourceLocation loc = new ResourceLocation(jsonReward.getString("rewardID"));
            int index = jsonReward.hasKey("index", 99) ? jsonReward.getInteger("index") : -1;
            IReward reward = RewardRegistry.INSTANCE.createNew(loc);

            if (reward instanceof RewardPlaceholder) {
                NBTTagCompound jr2 = jsonReward.getCompoundTag("orig_data");
                ResourceLocation loc2 = new ResourceLocation(jr2.getString("rewardID"));
                IReward r2 = RewardRegistry.INSTANCE.createNew(loc2);

                if (r2 != null) {
                    jsonReward = jr2;
                    reward = r2;
                }
            }

            if (reward != null) {
                reward.readFromNBT(jsonReward);

                if (index >= 0) {
                    add(index, reward);
                } else {
                    unassigned.add(reward);
                }
            } else {
                RewardPlaceholder rph = new RewardPlaceholder();
                rph.setRewardConfigData(jsonReward);

                if (index >= 0) {
                    add(index, rph);
                } else {
                    unassigned.add(rph);
                }
            }
        }

        for (IReward r : unassigned) {
            add(nextID(), r);
        }
    }

    // === Future support ===

    @Override
    public NBTTagList writeProgressToNBT(NBTTagList nbt, @Nullable List<UUID> user) {
        return nbt;
    }

    @Override
    public void readProgressFromNBT(NBTTagList nbt, boolean merge) {
    }
}
