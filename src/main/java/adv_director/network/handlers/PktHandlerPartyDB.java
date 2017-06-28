package adv_director.network.handlers;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import adv_director.api.events.DatabaseEvent;
import adv_director.api.network.IPacketHandler;
import adv_director.network.PacketSender;
import adv_director.network.PacketTypeNative;
import adv_director.questing.party.PartyManager;

public class PktHandlerPartyDB implements IPacketHandler
{
	@Override
	public ResourceLocation getRegistryName()
	{
		return PacketTypeNative.PARTY_DATABASE.GetLocation();
	}
	
	@Override
	public void handleServer(NBTTagCompound data, EntityPlayerMP sender) // Sync request
	{
		if(sender == null)
		{
			return;
		}
		
		PacketSender.INSTANCE.sendToPlayer(PartyManager.INSTANCE.getSyncPacket(), sender);
	}
	
	@Override
	public void handleClient(NBTTagCompound data)
	{
		PartyManager.INSTANCE.readPacket(data);
		MinecraftForge.EVENT_BUS.post(new DatabaseEvent.Update());
	}
}
