package betterquesting.network.handlers;

import betterquesting.api.network.IPacketHandler;
import betterquesting.api2.cache.CapabilityProviderQuestCache;
import betterquesting.api2.cache.QuestCache;
import betterquesting.network.PacketTypeNative;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

public class PktHandlerCacheSync implements IPacketHandler
{
    @Override
    public ResourceLocation getRegistryName()
    {
        return PacketTypeNative.CACHE_SYNC.GetLocation();
    }
    
    @Override
    public void handleServer(NBTTagCompound tag, EntityPlayerMP sender)
    {
    }
    
    @Override
    public void handleClient(NBTTagCompound tag)
    {
        EntityPlayer player = Minecraft.getMinecraft().thePlayer;
        QuestCache qc = player != null ? player.getCapability(CapabilityProviderQuestCache.CAP_QUEST_CACHE, null) : null;
        if(qc != null) qc.deserializeNBT(tag.getCompoundTag("data"));
    }
}