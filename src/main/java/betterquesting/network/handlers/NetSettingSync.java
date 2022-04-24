package betterquesting.network.handlers;

import betterquesting.api.api.QuestingAPI;
import betterquesting.api.network.QuestingPacket;
import betterquesting.core.BetterQuesting;
import betterquesting.handlers.SaveLoadHandler;
import betterquesting.network.PacketSender;
import betterquesting.network.PacketTypeRegistry;
import betterquesting.storage.QuestSettings;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.Level;

import javax.annotation.Nullable;

public class NetSettingSync {
    private static final ResourceLocation ID_NAME = new ResourceLocation("betterquesting:setting_sync");

    public static void registerHandler() {
        PacketTypeRegistry.INSTANCE.registerServerHandler(ID_NAME, NetSettingSync::onServer);

        if (BetterQuesting.proxy.isClient()) {
            PacketTypeRegistry.INSTANCE.registerClientHandler(ID_NAME, NetSettingSync::onClient);
        }
    }

    @SideOnly(Side.CLIENT)
    public static void requestEdit() {
        NBTTagCompound payload = new NBTTagCompound();
        payload.setTag("data", QuestSettings.INSTANCE.writeToNBT(new NBTTagCompound()));
        PacketSender.INSTANCE.sendToServer(new QuestingPacket(ID_NAME, payload));
    }

    public static void sendSync(@Nullable EntityPlayerMP player) {
        NBTTagCompound payload = new NBTTagCompound();
        payload.setTag("data", QuestSettings.INSTANCE.writeToNBT(new NBTTagCompound()));
        if (player != null) {
            PacketSender.INSTANCE.sendToPlayers(new QuestingPacket(ID_NAME, payload), player);
        } else {
            PacketSender.INSTANCE.sendToAll(new QuestingPacket(ID_NAME, payload));
        }
    }

    @SideOnly(Side.CLIENT)
    private static void onClient(NBTTagCompound message) {
        QuestSettings.INSTANCE.readFromNBT(message.getCompoundTag("data"));
    }

    private static void onServer(Tuple<NBTTagCompound, EntityPlayerMP> message) {
        MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
        if (!server.getPlayerList().canSendCommands(message.getSecond().getGameProfile())) {
            BetterQuesting.logger.log(Level.WARN, "Player " + message.getSecond().getName() + " (UUID:" + QuestingAPI.getQuestingUUID(message.getSecond()) + ") tried to edit settings without OP permissions!");
            sendSync(message.getSecond());
            return;
        }

        QuestSettings.INSTANCE.readFromNBT(message.getFirst().getCompoundTag("data"));
        SaveLoadHandler.INSTANCE.markDirty();
        sendSync(null);
    }
}
