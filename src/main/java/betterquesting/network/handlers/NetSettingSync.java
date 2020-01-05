package betterquesting.network.handlers;

import betterquesting.api.api.QuestingAPI;
import betterquesting.api.network.QuestingPacket;
import betterquesting.core.BetterQuesting;
import betterquesting.handlers.SaveLoadHandler;
import betterquesting.network.PacketSender;
import betterquesting.network.PacketTypeRegistry;
import betterquesting.storage.QuestSettings;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import org.apache.logging.log4j.Level;

import javax.annotation.Nullable;

public class NetSettingSync
{
    private static final ResourceLocation ID_NAME = new ResourceLocation("betterquesting:setting_sync");
    
    public static void registerHandler()
    {
        PacketTypeRegistry.INSTANCE.registerServerHandler(ID_NAME, NetSettingSync::onServer);
        
        if(BetterQuesting.isClient())
        {
            PacketTypeRegistry.INSTANCE.registerClientHandler(ID_NAME, NetSettingSync::onClient);
        }
    }
    
    @OnlyIn(Dist.CLIENT)
    public static void requestEdit()
    {
        CompoundNBT payload = new CompoundNBT();
        payload.put("data", QuestSettings.INSTANCE.writeToNBT(new CompoundNBT()));
        PacketSender.INSTANCE.sendToServer(new QuestingPacket(ID_NAME, payload));
    }
    
    public static void sendSync(@Nullable ServerPlayerEntity player)
    {
        CompoundNBT payload = new CompoundNBT();
        payload.put("data", QuestSettings.INSTANCE.writeToNBT(new CompoundNBT()));
        if(player != null)
        {
            PacketSender.INSTANCE.sendToPlayers(new QuestingPacket(ID_NAME, payload), player);
        } else
        {
            PacketSender.INSTANCE.sendToAll(new QuestingPacket(ID_NAME, payload));
        }
    }
    
    @OnlyIn(Dist.CLIENT)
    private static void onClient(CompoundNBT message)
    {
        QuestSettings.INSTANCE.readFromNBT(message.getCompound("data"));
    }
    
    private static void onServer(Tuple<CompoundNBT, ServerPlayerEntity> message)
    {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if(!server.getPlayerList().canSendCommands(message.getB().getGameProfile()))
        {
			BetterQuesting.logger.log(Level.WARN, "Player " + message.getB().getName() + " (UUID:" + QuestingAPI.getQuestingUUID(message.getB()) + ") tried to edit settings without OP permissions!");
            sendSync(message.getB());
            return;
        }
        
        QuestSettings.INSTANCE.readFromNBT(message.getA().getCompound("data"));
        SaveLoadHandler.INSTANCE.markDirty();
        sendSync(null);
    }
}
