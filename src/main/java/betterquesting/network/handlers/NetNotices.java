package betterquesting.network.handlers;

import betterquesting.api.network.QuestingPacket;
import betterquesting.client.QuestNotification;
import betterquesting.core.BetterQuesting;
import betterquesting.network.PacketSender;
import betterquesting.network.PacketTypeRegistry;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

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
    
    public static void sendNotice(@Nullable ServerPlayerEntity[] players, ItemStack icon, String mainText, String subText, String sound)
    {
        CompoundNBT payload = new CompoundNBT();
        payload.put("icon", (icon != null ? icon : ItemStack.EMPTY).write(new CompoundNBT()));
        if(mainText != null) payload.putString("mainText", mainText);
        if(subText != null) payload.putString("subText", subText);
        if(sound != null) payload.putString("sound", sound);
        
        if(players != null)
        {
            PacketSender.INSTANCE.sendToPlayers(new QuestingPacket(ID_NAME, payload), players);
        } else
        {
            PacketSender.INSTANCE.sendToAll(new QuestingPacket(ID_NAME, payload));
        }
    }
    
    @OnlyIn(Dist.CLIENT)
    private static void onClient(CompoundNBT message)
    {
		ItemStack stack = ItemStack.read(message.getCompound("icon"));
		String mainTxt = message.getString("mainText");
		String subTxt = message.getString("subText");
		String sound = message.getString("sound");
		QuestNotification.ScheduleNotice(mainTxt, subTxt, stack, sound);
    }
}
