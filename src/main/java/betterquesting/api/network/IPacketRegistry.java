package betterquesting.api.network;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Consumer;

public interface IPacketRegistry
{
    void registerServerHandler(@Nonnull ResourceLocation idName, @Nonnull Consumer<Tuple<CompoundNBT, ServerPlayerEntity>> method);
    
    @OnlyIn(Dist.CLIENT)
    void registerClientHandler(@Nonnull ResourceLocation idName, @Nonnull Consumer<CompoundNBT> method);
    
    @Nullable
    Consumer<Tuple<CompoundNBT,ServerPlayerEntity>> getServerHandler(@Nonnull ResourceLocation idName);
    
    @Nullable
    @OnlyIn(Dist.CLIENT)
    Consumer<CompoundNBT> getClientHandler(@Nonnull ResourceLocation idName);
}
