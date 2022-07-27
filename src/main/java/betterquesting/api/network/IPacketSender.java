package betterquesting.api.network;

import cpw.mods.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraft.entity.player.EntityPlayerMP;

public interface IPacketSender {
    // Server to Client
    void sendToPlayers(QuestingPacket payload, EntityPlayerMP... players);

    void sendToAll(QuestingPacket payload);

    // Client to Server
    void sendToServer(QuestingPacket payload);

    // Misc.
    void sendToAround(QuestingPacket payload, TargetPoint point);

    void sendToDimension(QuestingPacket payload, int dimension);
}
