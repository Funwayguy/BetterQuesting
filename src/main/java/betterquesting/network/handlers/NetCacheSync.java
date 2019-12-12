package betterquesting.network.handlers;

import betterquesting.api.network.QuestingPacket;
import betterquesting.api2.cache.CapabilityProviderQuestCache;
import betterquesting.api2.cache.QuestCache;
import betterquesting.core.BetterQuesting;
import betterquesting.network.PacketSender;
import betterquesting.network.PacketTypeRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;

public class NetCacheSync
{
    private static final ResourceLocation ID_NAME = new ResourceLocation("betterquesting:cache_sync");
    
    public static void registerHandler()
    {
        if(BetterQuesting.proxy.isClient())
        {
            PacketTypeRegistry.INSTANCE.registerClientHandler(ID_NAME, NetCacheSync::onClient);
        }
    }
    
    public static void sendSync(@Nonnull ServerPlayerEntity player)
    {
        QuestCache qc = player.getCapability(CapabilityProviderQuestCache.CAP_QUEST_CACHE, null).orElseGet(QuestCache::new);
        CompoundNBT payload = new CompoundNBT();
        payload.put("data", qc.serializeNBT());
        PacketSender.INSTANCE.sendToPlayers(new QuestingPacket(ID_NAME, payload), player);
    }
    
    @OnlyIn(Dist.CLIENT)
    private static void onClient(CompoundNBT message)
    {
        PlayerEntity player = Minecraft.getInstance().player;
        QuestCache qc = player != null ? player.getCapability(CapabilityProviderQuestCache.CAP_QUEST_CACHE, null).orElseGet(QuestCache::new) : null;
        if(qc != null) qc.deserializeNBT(message.getCompound("data"));
    }
}
