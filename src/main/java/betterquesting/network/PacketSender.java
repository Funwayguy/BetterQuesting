package betterquesting.network;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import betterquesting.api.network.IPacketSender;
import betterquesting.api.network.PreparedPayload;
import betterquesting.core.BetterQuesting;
import cpw.mods.fml.common.network.NetworkRegistry.TargetPoint;

public class PacketSender implements IPacketSender
{
	public static final PacketSender INSTANCE = new PacketSender();
	
	private PacketSender()
	{
	}
	
	@Override
	public void sendToPlayer(PreparedPayload payload, EntityPlayerMP player)
	{
		payload.getPayload().setString("ID", payload.getHandler().toString());
		
		for(NBTTagCompound p : PacketAssembly.INSTANCE.splitPacket(payload.getPayload()))
		{
			BetterQuesting.instance.network.sendTo(new PacketQuesting(p), player);
		}
	}
	
	@Override
	public void sendToAll(PreparedPayload payload)
	{
		payload.getPayload().setString("ID", payload.getHandler().toString());
		
		for(NBTTagCompound p : PacketAssembly.INSTANCE.splitPacket(payload.getPayload()))
		{
			BetterQuesting.instance.network.sendToAll(new PacketQuesting(p));
		}
	}
	
	@Override
	public void sendToServer(PreparedPayload payload)
	{
		payload.getPayload().setString("ID", payload.getHandler().toString());
		
		for(NBTTagCompound p : PacketAssembly.INSTANCE.splitPacket(payload.getPayload()))
		{
			BetterQuesting.instance.network.sendToServer(new PacketQuesting(p));
		}
	}
	
	@Override
	public void sendToAround(PreparedPayload payload, TargetPoint point)
	{
		payload.getPayload().setString("ID", payload.getHandler().toString());
		
		for(NBTTagCompound p : PacketAssembly.INSTANCE.splitPacket(payload.getPayload()))
		{
			BetterQuesting.instance.network.sendToAllAround(new PacketQuesting(p), point);
		}
	}
	
	@Override
	public void sendToDimension(PreparedPayload payload, int dimension)
	{
		payload.getPayload().setString("ID", payload.getHandler().toString());
		
		for(NBTTagCompound p : PacketAssembly.INSTANCE.splitPacket(payload.getPayload()))
		{
			BetterQuesting.instance.network.sendToDimension(new PacketQuesting(p), dimension);
		}
	}
}
