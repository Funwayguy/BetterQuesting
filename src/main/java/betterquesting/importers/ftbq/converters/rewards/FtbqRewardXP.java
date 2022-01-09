package betterquesting.importers.ftbq.converters.rewards;

import betterquesting.api.questing.rewards.IReward;
import betterquesting.questing.rewards.RewardXP;
import net.minecraft.nbt.NBTTagCompound;

public class FtbqRewardXP
{
    private final boolean isLevels;
    
    public FtbqRewardXP(boolean isLevels)
    {
        this.isLevels = isLevels;
    }
    
    public IReward[] convertTask(NBTTagCompound tag)
    {
        RewardXP reward = new RewardXP();
        reward.levels = this.isLevels;
        reward.amount = isLevels ? tag.getInteger("xp_levels") : tag.getInteger("xp");
        return new IReward[]{reward};
    }
}
