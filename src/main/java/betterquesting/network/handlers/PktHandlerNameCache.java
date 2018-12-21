package betterquesting.network.handlers;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import betterquesting.api.network.IPacketHandler;
import betterquesting.network.PacketTypeNative;
import betterquesting.storage.NameCache;

public class PktHandlerNameCache implements IPacketHandler
{
	@Override
	public ResourceLocation getRegistryName()
	{
		return PacketTypeNative.NAME_CACHE.GetLocation();
	}
	
	@Override
	public void handleServer(NBTTagCompound tag, EntityPlayerMP sender)
	{
		// Only server sends to clients
	}
	
	@Override
	public void handleClient(NBTTagCompound tag)
	{
		NameCache.INSTANCE.readPacket(tag);
	}
}
