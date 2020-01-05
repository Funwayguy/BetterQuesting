package betterquesting.network.handlers;

import betterquesting.api.api.QuestingAPI;
import betterquesting.api.events.DatabaseEvent;
import betterquesting.api.events.DatabaseEvent.DBType;
import betterquesting.api.network.QuestingPacket;
import betterquesting.core.BetterQuesting;
import betterquesting.network.PacketSender;
import betterquesting.network.PacketTypeRegistry;
import betterquesting.questing.party.PartyInvitations;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.UUID;

public class NetInviteSync
{
    private static final ResourceLocation ID_NAME = new ResourceLocation("betterquesting:invite_sync");
    
    public static void registerHandler()
    {
        if(BetterQuesting.isClient())
        {
            PacketTypeRegistry.INSTANCE.registerClientHandler(ID_NAME, NetInviteSync::onClient);
        }
    }
    
    // If I needt to send other people's invites to players then I'll deal with that another time
    public static void sendSync(@Nonnull ServerPlayerEntity player)
    {
        CompoundNBT payload = new CompoundNBT();
        UUID playerID = QuestingAPI.getQuestingUUID(player);
        payload.put("data", PartyInvitations.INSTANCE.writeToNBT(new ListNBT(), Collections.singletonList(playerID)));
        PacketSender.INSTANCE.sendToPlayers(new QuestingPacket(ID_NAME, payload), player);
    }
    
    @OnlyIn(Dist.CLIENT)
    private static void onClient(CompoundNBT message)
    {
        PartyInvitations.INSTANCE.readFromNBT(message.getList("data", 10), true);
        MinecraftForge.EVENT_BUS.post(new DatabaseEvent.Update(DBType.PARTY));
    }
}
