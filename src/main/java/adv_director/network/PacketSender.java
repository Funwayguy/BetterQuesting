package adv_director.network;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import adv_director.api.network.IPacketSender;
import adv_director.api.network.QuestingPacket;
import adv_director.core.AdvDirector;

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
			AdvDirector.instance.network.sendTo(new PacketQuesting(p), player);
		}
	}
	
	@Override
	public void sendToAll(QuestingPacket payload)
	{
		payload.getPayload().setString("ID", payload.getHandler().toString());
		
		for(NBTTagCompound p : PacketAssembly.INSTANCE.splitPacket(payload.getPayload()))
		{
			AdvDirector.instance.network.sendToAll(new PacketQuesting(p));
		}
	}
	
	@Override
	public void sendToServer(QuestingPacket payload)
	{
		payload.getPayload().setString("ID", payload.getHandler().toString());
		
		for(NBTTagCompound p : PacketAssembly.INSTANCE.splitPacket(payload.getPayload()))
		{
			AdvDirector.instance.network.sendToServer(new PacketQuesting(p));
		}
	}
	
	@Override
	public void sendToAround(QuestingPacket payload, TargetPoint point)
	{
		payload.getPayload().setString("ID", payload.getHandler().toString());
		
		for(NBTTagCompound p : PacketAssembly.INSTANCE.splitPacket(payload.getPayload()))
		{
			AdvDirector.instance.network.sendToAllAround(new PacketQuesting(p), point);
		}
	}
	
	@Override
	public void sendToDimension(QuestingPacket payload, int dimension)
	{
		payload.getPayload().setString("ID", payload.getHandler().toString());
		
		for(NBTTagCompound p : PacketAssembly.INSTANCE.splitPacket(payload.getPayload()))
		{
			AdvDirector.instance.network.sendToDimension(new PacketQuesting(p), dimension);
		}
	}
}
