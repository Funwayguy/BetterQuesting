package betterquesting.questing.rewards;

import betterquesting.api.placeholders.rewards.FactoryRewardPlaceholder;
import betterquesting.api.questing.rewards.IReward;
import betterquesting.api2.registry.IFactoryData;
import betterquesting.api2.registry.IRegistry;
import betterquesting.core.BetterQuesting;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import org.apache.logging.log4j.Level;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class RewardRegistry implements IRegistry<IFactoryData<IReward, NBTTagCompound>, IReward> {
    public static final RewardRegistry INSTANCE = new RewardRegistry();

    private final HashMap<ResourceLocation, IFactoryData<IReward, NBTTagCompound>> rewardRegistry = new HashMap<>();

    @Override
    public void register(IFactoryData<IReward, NBTTagCompound> factory) {
        if (factory == null) {
            throw new NullPointerException("Tried to register null reward");
        } else if (factory.getRegistryName() == null) {
            throw new IllegalArgumentException("Tried to register a reward with a null name: " + factory.getClass());
        }

        if (rewardRegistry.containsKey(factory.getRegistryName()) || rewardRegistry.containsValue(factory)) {
            throw new IllegalArgumentException("Cannot register dupliate reward type: " + factory.getRegistryName());
        }

        rewardRegistry.put(factory.getRegistryName(), factory);
    }

    @Override
    public IFactoryData<IReward, NBTTagCompound> getFactory(ResourceLocation registryName) {
        return rewardRegistry.get(registryName);
    }

    @Override
    public List<IFactoryData<IReward, NBTTagCompound>> getAll() {
        return new ArrayList<>(rewardRegistry.values());
    }

    @Override
    public IReward createNew(ResourceLocation registryName) {
        try {
            IFactoryData<? extends IReward, NBTTagCompound> factory;

            if (FactoryRewardPlaceholder.INSTANCE.getRegistryName().equals(registryName)) {
                factory = FactoryRewardPlaceholder.INSTANCE;
            } else {
                factory = getFactory(registryName);
            }

            if (factory == null) {
                BetterQuesting.logger.log(Level.ERROR, "Tried to load missing reward type '" + registryName + "'! Are you missing an expansion pack?");
                return null;
            }

            return factory.createNew();
        } catch (Exception e) {
            BetterQuesting.logger.log(Level.ERROR, "Unable to instatiate reward: " + registryName, e);
            return null;
        }
    }
}
