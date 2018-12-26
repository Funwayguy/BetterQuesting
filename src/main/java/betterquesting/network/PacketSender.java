package betterquesting.network;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import betterquesting.api.api.ApiReference;
import betterquesting.api.api.QuestingAPI;
import betterquesting.api.enums.EnumPartyStatus;
import betterquesting.api.network.IPacketSender;
import betterquesting.api.network.QuestingPacket;
import betterquesting.api.questing.party.IParty;
import betterquesting.api.questing.party.IPartyDatabase;
import betterquesting.api.storage.INameCache;
import betterquesting.core.BetterQuesting;

public class PacketSender implements IPacketSender
{
	public static final PacketSender INSTANCE = new PacketSender();
	
	private PacketSender()
	{
	}
	
	@Override
	public void sendToPlayer(QuestingPacket payload, EntityPlayerMP player)
	{
		payload.getPayload().setString("ID", payload.getHandler().toString());
		
		for(NBTTagCompound p : PacketAssembly.INSTANCE.splitPacket(payload.getPayload()))
		{
			BetterQuesting.instance.network.sendTo(new PacketQuesting(p), player);
		}
	}
	
	public void sendToParty(QuestingPacket payload, EntityPlayer player)
	{
		INameCache names = QuestingAPI.getAPI(ApiReference.NAME_CACHE);
		sendToParty(payload, player.getServer(), names.registerAndGetUUID(player));
	}
	
	public void sendToParty(QuestingPacket payload, MinecraftServer server, UUID player)
	{
		IPartyDatabase partys = QuestingAPI.getAPI(ApiReference.PARTY_DB);
		IParty userParty = partys.getUserParty(player);
		if(userParty != null)
		{
			sendToParty(payload, server, userParty);
		} else
		{
			INameCache names = QuestingAPI.getAPI(ApiReference.NAME_CACHE);
			sendToPlayersIfOnline(payload, server, Collections.singletonList(names.getName(player)));
		}
	}
	
	public void sendToParty(QuestingPacket payload, MinecraftServer server, IParty party)
	{
		INameCache names = QuestingAPI.getAPI(ApiReference.NAME_CACHE);
		sendToPlayersIfOnline(payload, server, party.getMembers().stream()
				.filter(u -> u != null && party.getStatus(u) != EnumPartyStatus.INVITE)
				.map(u -> names.getName(u)).collect(Collectors.toList()));
	}
	
	public void sendToPlayersIfOnline(QuestingPacket payload, MinecraftServer server, List<String> players)
	{
		payload.getPayload().setString("ID", payload.getHandler().toString());
		for(NBTTagCompound p : PacketAssembly.INSTANCE.splitPacket(payload.getPayload()))
		{
			// perhaps it would make sense to loop over players variable but the server does it anyway when
			// calling getUserByName. Since we have our own name->uuid mapping, we can't use the servers getPlayerForUUID which is a map
			for(EntityPlayerMP player : server.getPlayerList().getPlayers())
			{
				if(!players.contains(player.getName())) {
					continue;
				}
				BetterQuesting.instance.network.sendTo(new PacketQuesting(p), player);
			}
		}
	}
	
	@Override
	public void sendToAll(QuestingPacket payload)
	{
		payload.getPayload().setString("ID", payload.getHandler().toString());
		
		for(NBTTagCompound p : PacketAssembly.INSTANCE.splitPacket(payload.getPayload()))
		{
			BetterQuesting.instance.network.sendToAll(new PacketQuesting(p));
		}
	}
	
	@Override
	public void sendToServer(QuestingPacket payload)
	{
		payload.getPayload().setString("ID", payload.getHandler().toString());
		
		for(NBTTagCompound p : PacketAssembly.INSTANCE.splitPacket(payload.getPayload()))
		{
			BetterQuesting.instance.network.sendToServer(new PacketQuesting(p));
		}
	}
	
	@Override
	public void sendToAround(QuestingPacket payload, TargetPoint point)
	{
		payload.getPayload().setString("ID", payload.getHandler().toString());
		
		for(NBTTagCompound p : PacketAssembly.INSTANCE.splitPacket(payload.getPayload()))
		{
			BetterQuesting.instance.network.sendToAllAround(new PacketQuesting(p), point);
		}
	}
	
	@Override
	public void sendToDimension(QuestingPacket payload, int dimension)
	{
		payload.getPayload().setString("ID", payload.getHandler().toString());
		
		for(NBTTagCompound p : PacketAssembly.INSTANCE.splitPacket(payload.getPayload()))
		{
			BetterQuesting.instance.network.sendToDimension(new PacketQuesting(p), dimension);
		}
	}
}
