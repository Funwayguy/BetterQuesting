package betterquesting.api2.cache;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.*;
import net.minecraftforge.common.capabilities.Capability.IStorage;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CapabilityProviderQuestCache implements ICapabilityProvider, ICapabilitySerializable<CompoundNBT>
{
    @CapabilityInject(QuestCache.class)
    public static Capability<QuestCache> CAP_QUEST_CACHE;
    public static final ResourceLocation LOC_QUEST_CACHE = new ResourceLocation("betterquesting", "quest_cache");
    
    private final LazyOptional<QuestCache> cache = LazyOptional.of(QuestCache::new);
    
    @Override
    public CompoundNBT serializeNBT()
    {
        return cache.orElseGet(QuestCache::new).serializeNBT();
    }
    
    @Override
    public void deserializeNBT(CompoundNBT nbt)
    {
        cache.orElseGet(QuestCache::new).deserializeNBT(nbt);
    }
    
    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction facing)
    {
        return capability == CAP_QUEST_CACHE ? cache.cast() : LazyOptional.empty();
    }
    
    public static void register()
    {
        CapabilityManager.INSTANCE.register(QuestCache.class, new IStorage<QuestCache>()
        {
            @Nullable
            @Override
            public INBT writeNBT(Capability<QuestCache> capability, QuestCache instance, Direction side)
            {
                return instance.serializeNBT();
            }
    
            @Override
            public void readNBT(Capability<QuestCache> capability, QuestCache instance, Direction side, INBT nbt)
            {
                if(nbt instanceof CompoundNBT) instance.deserializeNBT((CompoundNBT)nbt);
            }
        }, QuestCache::new);
    }
}
