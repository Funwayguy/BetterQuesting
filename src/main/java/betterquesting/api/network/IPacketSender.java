package betterquesting.api.network;

import java.util.List;
import java.util.UUID;

import betterquesting.api.questing.party.IParty;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;

public interface IPacketSender
{
	public void sendToPlayer(QuestingPacket payload, EntityPlayerMP player);
	public void sendToAll(QuestingPacket payload);
	public void sendToServer(QuestingPacket payload);
	
	public void sendToAround(QuestingPacket payload, TargetPoint point);
	public void sendToDimension(QuestingPacket payload, int dimension);

	public void sendToParty(QuestingPacket payload, EntityPlayer player);
	public void sendToParty(QuestingPacket payload, MinecraftServer server, UUID player);
	public void sendToParty(QuestingPacket payload, MinecraftServer server, IParty party);
	public void sendToPlayersIfOnline(QuestingPacket payload, MinecraftServer server, List<String> players);
}
