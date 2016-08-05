package betterquesting.api.network;

import cpw.mods.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

public interface IPacketSender
{
	public void sendToPlayer(ResourceLocation handler, NBTTagCompound payload, EntityPlayerMP player);
	public void sendToAll(ResourceLocation handler, NBTTagCompound payload);
	public void sendToServer(ResourceLocation handler, NBTTagCompound payload);
	
	public void sendToAround(ResourceLocation handler, NBTTagCompound payload, TargetPoint point);
	public void sendToDimension(ResourceLocation handler, NBTTagCompound payload, int dimension);
}
