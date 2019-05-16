package betterquesting.api.network;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;

public interface IPacketSender
{
    // Server to Client
	void sendToPlayer(QuestingPacket payload, EntityPlayerMP player);
	void sendToAll(QuestingPacket payload);
	//void sendToUsers(QuestingPacket payload, List<UUID> users);
	
	// Client to Server
	void sendToServer(QuestingPacket payload);
	
	// Misc.
	void sendToAround(QuestingPacket payload, TargetPoint point);
	void sendToDimension(QuestingPacket payload, int dimension);
}
