package betterquesting.api.network;

import net.minecraft.entity.player.EntityPlayerMP;
import cpw.mods.fml.common.network.NetworkRegistry.TargetPoint;

public interface IPacketSender
{
	public void sendToPlayer(QuestingPacket payload, EntityPlayerMP player);
	public void sendToAll(QuestingPacket payload);
	public void sendToServer(QuestingPacket payload);
	
	public void sendToAround(QuestingPacket payload, TargetPoint point);
	public void sendToDimension(QuestingPacket payload, int dimension);
}
