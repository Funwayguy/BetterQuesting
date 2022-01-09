package betterquesting.importers.ftbq.converters.rewards;

import betterquesting.api.questing.rewards.IReward;
import betterquesting.questing.rewards.RewardCommand;
import net.minecraft.nbt.NBTTagCompound;

public class FtbqRewardCommand
{
    public IReward[] convertReward(NBTTagCompound tag)
    {
        RewardCommand reward = new RewardCommand();
        reward.viaPlayer = false; // FTBQ only runs as server
        reward.desc = tag.getString("title");
        reward.command = tag.getString("command");
        return new IReward[]{reward};
    }
}
