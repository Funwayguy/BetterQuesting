package betterquesting.network.handlers;

import betterquesting.api.network.QuestingPacket;
import betterquesting.core.BetterQuesting;
import betterquesting.network.PacketSender;
import betterquesting.network.PacketTypeRegistry;
import betterquesting.storage.LifeDatabase;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.UUID;

public class NetLifeSync
{
    private static final ResourceLocation ID_NAME = new ResourceLocation("betterquesting:life_sync");
    
    public static void registerHandler()
    {
        if(BetterQuesting.isClient())
        {
            PacketTypeRegistry.INSTANCE.registerClientHandler(ID_NAME, NetLifeSync::onClient);
        }
    }
    
    public static void sendSync(@Nullable ServerPlayerEntity[] players, @Nullable UUID[] playerIDs)
    {
        CompoundNBT payload = new CompoundNBT();
        payload.put("data", LifeDatabase.INSTANCE.writeToNBT(new CompoundNBT(), playerIDs == null ? null : Arrays.asList(playerIDs)));
        payload.putBoolean("merge", playerIDs != null);
        
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
        LifeDatabase.INSTANCE.readFromNBT(message.getCompound("data"), message.getBoolean("merge"));
    }
}
