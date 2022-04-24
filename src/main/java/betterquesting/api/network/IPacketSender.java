package betterquesting.api.network;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;

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
