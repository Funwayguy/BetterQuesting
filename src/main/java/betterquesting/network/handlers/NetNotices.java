package betterquesting.network.handlers;

import betterquesting.api.network.QuestingPacket;
import betterquesting.client.QuestNotification;
import betterquesting.core.BetterQuesting;
import betterquesting.network.PacketSender;
import betterquesting.network.PacketTypeRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;

public class NetNotices
{
    // TODO: Convert over to inbox system in future
    private static final ResourceLocation ID_NAME = new ResourceLocation("betterquesting:notification");
    
    public static void registerHandler()
    {
        if(BetterQuesting.proxy.isClient())
        {
            PacketTypeRegistry.INSTANCE.registerClientHandler(ID_NAME, NetNotices::onClient);
        }
    }
    
    public static void sendNotice(@Nullable EntityPlayerMP[] players, ItemStack icon, String mainText, String subText, String sound)
    {
        NBTTagCompound payload = new NBTTagCompound();
        payload.setTag("icon", icon == null ? new NBTTagCompound() : icon.writeToNBT(new NBTTagCompound()));
        if(mainText != null) payload.setString("mainText", mainText);
        if(subText != null) payload.setString("subText", subText);
        if(sound != null) payload.setString("sound", sound);
        
        if(players != null)
        {
            PacketSender.INSTANCE.sendToPlayers(new QuestingPacket(ID_NAME, payload), players);
        } else
        {
            PacketSender.INSTANCE.sendToAll(new QuestingPacket(ID_NAME, payload));
        }
    }
    
    @SideOnly(Side.CLIENT)
    private static void onClient(NBTTagCompound message)
    {
		ItemStack stack = ItemStack.loadItemStackFromNBT(message.getCompoundTag("icon"));
		String mainTxt = message.getString("mainText");
		String subTxt = message.getString("subText");
		String sound = message.getString("sound");
		QuestNotification.ScheduleNotice(mainTxt, subTxt, stack, sound);
    }
}
