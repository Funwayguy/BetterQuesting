package betterquesting.api.network;

import net.minecraft.entity.player.EntityPlayerMP;
import cpw.mods.fml.common.network.NetworkRegistry.TargetPoint;

public interface IPacketSender
{
	public void sendToPlayer(PreparedPayload payload, EntityPlayerMP player);
	public void sendToAll(PreparedPayload payload);
	public void sendToServer(PreparedPayload payload);
	
	public void sendToAround(PreparedPayload payload, TargetPoint point);
	public void sendToDimension(PreparedPayload payload, int dimension);
}
