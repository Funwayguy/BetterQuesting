package betterquesting.api.placeholders.rewards;

import betterquesting.api2.registry.IFactoryData;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

public class FactoryRewardPlaceholder implements IFactoryData<RewardPlaceholder, NBTTagCompound> {
    public static final FactoryRewardPlaceholder INSTANCE = new FactoryRewardPlaceholder();

    private final ResourceLocation ID = new ResourceLocation("betterquesting:placeholder");

    private FactoryRewardPlaceholder() {}

    @Override
    public ResourceLocation getRegistryName() {
        return ID;
    }

    @Override
    public RewardPlaceholder createNew() {
        return new RewardPlaceholder();
    }

    @Override
    public RewardPlaceholder loadFromData(NBTTagCompound nbt) {
        RewardPlaceholder reward = createNew();
        reward.readFromNBT(nbt);
        return reward;
    }
}
