package betterquesting.network.handlers;

import betterquesting.api.network.QuestingPacket;
import betterquesting.api2.cache.QuestCache;
import betterquesting.core.BetterQuesting;
import betterquesting.network.PacketSender;
import betterquesting.network.PacketTypeRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

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
    
    public static void sendSync(@Nonnull EntityPlayerMP player)
    {
        QuestCache qc = (QuestCache)player.getExtendedProperties(QuestCache.LOC_QUEST_CACHE.toString());
        if(qc == null) return;
        NBTTagCompound payload = new NBTTagCompound();
        NBTTagCompound data = new NBTTagCompound();
        qc.saveNBTData(data);
        payload.setTag("data", data);
        PacketSender.INSTANCE.sendToPlayers(new QuestingPacket(ID_NAME, payload), player);
    }
    
    @SideOnly(Side.CLIENT)
    private static void onClient(NBTTagCompound message)
    {
        EntityPlayer player = Minecraft.getMinecraft().thePlayer;
        QuestCache qc = player == null ? null : (QuestCache)player.getExtendedProperties(QuestCache.LOC_QUEST_CACHE.toString());
        if(qc != null) qc.loadNBTData(message.getCompoundTag("data"));
    }
}
