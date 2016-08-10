package betterquesting.network;

import cpw.mods.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import betterquesting.api.network.IPacketSender;
import betterquesting.core.BetterQuesting;

public class PacketSender implements IPacketSender
{
	public static final PacketSender INSTANCE = new PacketSender();
	
	private PacketSender()
	{
	}
	
	@Override
	public void sendToPlayer(ResourceLocation handler, NBTTagCompound payload, EntityPlayerMP player)
	{
		payload.setString("ID", handler.toString());
		
		for(NBTTagCompound p : PacketAssembly.INSTANCE.splitPacket(payload))
		{
			BetterQuesting.instance.network.sendTo(new PacketQuesting(p), player);
		}
	}
	
	@Override
	public void sendToAll(ResourceLocation handler, NBTTagCompound payload)
	{
		payload.setString("ID", handler.toString());
		
		for(NBTTagCompound p : PacketAssembly.INSTANCE.splitPacket(payload))
		{
			BetterQuesting.instance.network.sendToAll(new PacketQuesting(p));
		}
	}
	
	@Override
	public void sendToServer(ResourceLocation handler, NBTTagCompound payload)
	{
		payload.setString("ID", handler.toString());
		
		for(NBTTagCompound p : PacketAssembly.INSTANCE.splitPacket(payload))
		{
			BetterQuesting.instance.network.sendToServer(new PacketQuesting(p));
		}
	}
	
	@Override
	public void sendToAround(ResourceLocation handler, NBTTagCompound payload, TargetPoint point)
	{
		payload.setString("ID", handler.toString());
		
		for(NBTTagCompound p : PacketAssembly.INSTANCE.splitPacket(payload))
		{
			BetterQuesting.instance.network.sendToAllAround(new PacketQuesting(p), point);
		}
	}
	
	@Override
	public void sendToDimension(ResourceLocation handler, NBTTagCompound payload, int dimension)
	{
		payload.setString("ID", handler.toString());
		
		for(NBTTagCompound p : PacketAssembly.INSTANCE.splitPacket(payload))
		{
			BetterQuesting.instance.network.sendToDimension(new PacketQuesting(p), dimension);
		}
	}
}
