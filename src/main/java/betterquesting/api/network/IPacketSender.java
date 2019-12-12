package betterquesting.api.network;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraftforge.fml.network.PacketDistributor.TargetPoint;

public interface IPacketSender
{
    // Server to Client
    void sendToPlayers(QuestingPacket payload, ServerPlayerEntity... players);
	void sendToAll(QuestingPacket payload);
	
	// Client to Server
	void sendToServer(QuestingPacket payload);
	
	// Misc.
	void sendToAround(QuestingPacket payload, TargetPoint point);
	void sendToDimension(QuestingPacket payload, int dimension);
}
